package com.xianxia.qunxia.game.engine

import android.util.Log
import com.xianxia.qunxia.data.repository.GameRepository
import com.xianxia.qunxia.game.npc.NpcProfile
import com.xianxia.qunxia.game.world.WorldState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 世界引擎 —— 游戏的主循环
 *
 * 职责：
 * 1. 管理游戏内时间推进
 * 2. 调度每个 tick 的更新
 * 3. 管理 NPC 决策时机
 * 4. 处理离线回归时的批量演算
 */
class WorldEngine(
    private val repository: GameRepository,
    private val npcDecisionMaker: NpcDecisionMaker
) {
    companion object {
        private const val TAG = "WorldEngine"
    }

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var tickJob: Job? = null

    private val _worldState = MutableStateFlow<WorldState?>(null)
    val worldState: StateFlow<WorldState?> = _worldState.asStateFlow()

    private val _events = MutableStateFlow<List<GameEvent>>(emptyList())
    val recentEvents: StateFlow<List<GameEvent>> = _events.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    // 离线期间生成的事件缓存（等玩家上线后展示）
    private val pendingOfflineEvents = mutableListOf<GameEvent>()

    // ========== 生命周期 ==========

    /**
     * 启动游戏：加载存档或新建游戏
     */
    suspend fun startGame(playerName: String): Result<WorldState> = try {
        val saved = repository.loadWorld()
        val state = if (saved != null) {
            // 处理离线回归——批量演算
            handleOfflineReturn(saved)
            saved
        } else {
            repository.createNewGame(playerName)
        }
        _worldState.value = state
        Result.success(state)
    } catch (e: Exception) {
        Log.e(TAG, "启动游戏失败", e)
        Result.failure(e)
    }

    /**
     * 开始 tick 循环
     */
    fun startTickLoop() {
        if (_isRunning.value) return
        _isRunning.value = true
        tickJob = scope.launch {
            while (isActive) {
                val state = _worldState.value ?: break
                tick(state)
                _worldState.value = state

                // 推送最近的事件到 UI
                _events.value = state.eventLog.takeLast(50).reversed()

                delay(state.tickIntervalMs)
            }
        }
    }

    /**
     * 暂停 tick 循环
     */
    fun stopTickLoop() {
        tickJob?.cancel()
        tickJob = null
        _isRunning.value = false
    }

    /**
     * 游戏存档
     */
    suspend fun saveGame() {
        val state = _worldState.value ?: return
        repository.saveWorld(state)
    }

    /**
     * 手动触发一次 NPC 决策（用于玩家与 NPC 交互时）
     */
    suspend fun triggerNpcDecision(npcId: String): GameEvent? {
        val state = _worldState.value ?: return null
        val npc = state.npcs[npcId] ?: return null
        if (!npc.isCoreAgent) return null

        val event = npcDecisionMaker.makeDecision(npc, state)
        if (event != null) {
            state.eventLog.add(event)
            npc.totalDecisions++
            _events.value = state.eventLog.takeLast(50).reversed()
        }
        return event
    }

    /**
     * 获取未读的重要事件
     */
    fun getUnreadEvents(): List<GameEvent> {
        return _worldState.value?.eventLog?.filter { !it.isRead } ?: emptyList()
    }

    /**
     * 标记事件为已读
     */
    fun markEventsRead(eventIds: List<String>) {
        _worldState.value?.eventLog?.forEach { event ->
            if (event.id in eventIds) event.isRead = true
        }
    }

    fun shutdown() {
        stopTickLoop()
        scope.cancel()
    }

    // ========== 内部逻辑 ==========

    /**
     * 单个 tick 推进
     */
    private suspend fun tick(state: WorldState) {
        // 1. 推进时间
        advanceTime(state)

        // 2. 更新路人NPC
        updateBackgroundNpcs(state)

        // 3. 判断核心NPC是否需要决策
        checkNpcDecisions(state)

        // 4. 宗门状态更新
        updateFactions(state)

        // 5. 随机世界事件判定
        checkRandomEvents(state)
    }

    private fun advanceTime(state: WorldState) {
        val hoursPerTick = 1  // 每个tick推进游戏内1小时
        state.gameHour += hoursPerTick
        state.totalTicks++

        if (state.gameHour >= 24) {
            state.gameHour -= 24
            state.gameDay++
            onNewDay(state)
        }
    }

    private fun onNewDay(state: WorldState) {
        // 每天重置一些状态
        // 目前留空，后续可添加：每月结算、季节变化等
    }

    /**
     * 更新路人NPC（行为树驱动，无LLM）
     */
    private fun updateBackgroundNpcs(state: WorldState) {
        for ((_, npc) in state.npcs) {
            if (npc.isCoreAgent) continue  // 核心NPC由LLM驱动

            // 简单的行为轮换
            when (npc.currentAction) {
                "修炼" -> {
                    // 小概率触发突破事件
                    if (shouldTrigger(0.05)) {
                        state.eventLog.add(GameEvent(
                            id = generateEventId(),
                            gameDay = state.gameDay,
                            gameHour = state.gameHour,
                            type = EventType.NPC_BREAKTHROUGH,
                            title = "${npc.name}略有感悟",
                            description = "${npc.name}修炼有所精进，气息更加凝实了。",
                            participants = listOf(npc.id),
                            location = npc.location,
                            importance = 1
                        ))
                    }
                }
                "闲逛" -> {
                    if (shouldTrigger(0.02)) {
                        // 闲逛触发小事件
                    }
                }
            }

            // 随机切换行为
            if (shouldTrigger(0.1)) {
                npc.currentAction = listOf("修炼", "闲逛", "社交", "休息").random()
            }
        }
    }

    /**
     * 检查核心NPC是否需要做决策
     */
    private suspend fun checkNpcDecisions(state: WorldState) {
        for ((_, npc) in state.npcs) {
            if (!npc.isCoreAgent || !npc.isAlive) continue

            if (state.gameDay - npc.lastDecisionDay >= npc.decisionFrequency) {
                // 经验：非活跃NPC（离主角远且在安全区）可降频
                val effectiveFrequency = if (isNpcNearPlayer(npc, state)) {
                    1  // 靠近主角的正常频率
                } else {
                    npc.decisionFrequency * 2  // 远离主角的降频
                }

                if (state.gameDay - npc.lastDecisionDay >= effectiveFrequency) {
                    val event = npcDecisionMaker.makeDecision(npc, state)
                    if (event != null) {
                        state.eventLog.add(event)
                    }
                    npc.lastDecisionDay = state.gameDay
                    npc.totalDecisions++
                }
            }
        }
    }

    /**
     * 宗门状态更新
     */
    private fun updateFactions(state: WorldState) {
        for ((_, faction) in state.factions) {
            if (!faction.isActive) continue

            // 宗门资源自然增长
            if (state.gameHour == 0) {  // 每天一次
                faction.wealth = (faction.wealth * 1.01).toInt().coerceAtMost(1000)
            }
        }
    }

    /**
     * 随机世界事件
     */
    private fun checkRandomEvents(state: WorldState) {
        // 每天有一定概率发生世界事件
        if (state.gameHour == 6 && shouldTrigger(0.15)) {  // 每天6点判定一次，15%概率
            val event = generateRandomWorldEvent(state)
            state.eventLog.add(event)
        }
    }

    /**
     * 离线回归处理——批量演算离线期间发生的事
     */
    private suspend fun handleOfflineReturn(state: WorldState) {
        val now = System.currentTimeMillis()
        val lastSave = repository.loadWorld()?.let {
            // 这里简化处理：从上次游戏时间推算离线时长
            // 实际应用中需要记录上次的实时时间戳
            0L
        } ?: return

        // 如果离线超过一定时间，进行批量演算
        // 简化实现：根据离线时间推进游戏天数
        val offlineHours = 1L  // 简化，实际应计算
        if (offlineHours < 1) return

        val offlineGameDays = (offlineHours * state.timeScale / 24).toInt()
        val simulatedDays = offlineGameDays.coerceIn(1, 30)  // 最多演算30天

        Log.d(TAG, "离线演算: $simulatedDays 游戏日")

        // 批量演算
        for (day in 1..simulatedDays) {
            state.gameDay++

            // 核心NPC决策（降频）
            for ((_, npc) in state.npcs) {
                if (!npc.isCoreAgent || !npc.isAlive) continue
                // 离线期间每3天决策一次
                if (day % 3 == 0 && shouldTrigger(0.7)) {
                    val event = npcDecisionMaker.makeDecision(npc, state)
                    if (event != null) {
                        event.description = "【离线期间】" + event.description
                        pendingOfflineEvents.add(event)
                        state.eventLog.add(event)
                    }
                    npc.lastDecisionDay = state.gameDay
                    npc.totalDecisions++
                }
            }

            // 宗门关系缓慢变化
            for ((_, faction) in state.factions) {
                if (!faction.isActive) continue
                // 随机波动
                for ((target, rel) in faction.relations) {
                    if (shouldTrigger(0.1)) {
                        rel.value = (rel.value + (-5..5).random()).coerceIn(-100, 100)
                    }
                }
            }
        }

        // 标记离线事件
        Log.d(TAG, "离线演算完成，产生 ${pendingOfflineEvents.size} 个事件")
    }

    // ========== 辅助方法 ==========

    private fun isNpcNearPlayer(npc: NpcProfile, state: WorldState): Boolean {
        return npc.location == state.player.location
    }

    private fun generateRandomWorldEvent(state: WorldState): GameEvent {
        val events = listOf(
            GameEvent(
                id = generateEventId(), gameDay = state.gameDay,
                gameHour = state.gameHour, type = EventType.WORLD_SECRET,
                title = "秘境传闻",
                description = "修仙界传闻在某地发现了一处上古秘境，各派摩拳擦掌准备探索。",
                importance = 3
            ),
            GameEvent(
                id = generateEventId(), gameDay = state.gameDay,
                gameHour = state.gameHour, type = EventType.WORLD_TREASURE,
                title = "天材地宝出世",
                description = "有采药人在深山中发现了罕见的千年灵芝，消息走漏后引起多人争抢。",
                importance = 2
            ),
            GameEvent(
                id = generateEventId(), gameDay = state.gameDay,
                gameHour = state.gameHour, type = EventType.FACTION_FEAST,
                title = "宗门庆典",
                description = "某个宗门正在举办盛大的庆典，广邀天下修士参加。",
                importance = 1
            )
        )
        return events.random()
    }

    private var eventCounter = 0
    private fun generateEventId(): String {
        eventCounter++
        return "evt_${System.currentTimeMillis()}_$eventCounter"
    }

    private fun shouldTrigger(probability: Double): Boolean {
        return Math.random() < probability
    }
}

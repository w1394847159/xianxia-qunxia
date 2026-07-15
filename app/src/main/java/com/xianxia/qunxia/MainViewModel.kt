package com.xianxia.qunxia

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.xianxia.qunxia.agent.LlmClient
import com.xianxia.qunxia.agent.TokenEstimator
import com.xianxia.qunxia.agent.TokenTracker
import com.xianxia.qunxia.data.repository.GameRepository
import com.xianxia.qunxia.game.engine.GameEvent
import com.xianxia.qunxia.game.engine.NpcDecisionMaker
import com.xianxia.qunxia.game.engine.WorldEngine
import com.xianxia.qunxia.game.faction.Faction
import com.xianxia.qunxia.game.npc.NpcProfile
import com.xianxia.qunxia.game.world.WorldState
import com.xianxia.qunxia.settings.ApiConfig
import com.xianxia.qunxia.settings.SettingsManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * 主 ViewModel —— 管理所有游戏状态和 UI 状态
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as XianXiaApp
    val settingsManager: SettingsManager = app.settingsManager
    val repository: GameRepository = app.repository
    val worldEngine: WorldEngine = app.worldEngine
    val tokenTracker: TokenTracker = app.tokenTracker

    // ========== UI 状态 ==========

    /** 当前屏幕 */
    private val _currentScreen = MutableStateFlow(Screen.LOADING)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    /** 世界状态 */
    val worldState: StateFlow<WorldState?> = worldEngine.worldState

    /** 最近事件 */
    val recentEvents: StateFlow<List<GameEvent>> = worldEngine.recentEvents

    /** 游戏是否在运行 */
    val isGameRunning: StateFlow<Boolean> = worldEngine.isRunning

    /** Token 统计 */
    val tokenStats = tokenTracker.stats

    /** 选中的 NPC */
    private val _selectedNpc = MutableStateFlow<NpcProfile?>(null)
    val selectedNpc: StateFlow<NpcProfile?> = _selectedNpc.asStateFlow()

    /** 选中的宗门 */
    private val _selectedFaction = MutableStateFlow<Faction?>(null)
    val selectedFaction: StateFlow<Faction?> = _selectedFaction.asStateFlow()

    /** 错误消息 */
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /** API 配置 */
    private val _apiConfig = MutableStateFlow(ApiConfig())
    val apiConfig: StateFlow<ApiConfig> = _apiConfig.asStateFlow()

    /** Token 预估 */
    private val _estimation = MutableStateFlow<TokenEstimator.Estimation?>(null)
    val estimation: StateFlow<TokenEstimator.Estimation?> = _estimation.asStateFlow()

    /** 可用模型列表（从用户 API 动态获取） */
    private val _availableModels = MutableStateFlow<List<String>>(emptyList())
    val availableModels: StateFlow<List<String>> = _availableModels.asStateFlow()

    private val _isLoadingModels = MutableStateFlow(false)
    val isLoadingModels: StateFlow<Boolean> = _isLoadingModels.asStateFlow()

    /** NPC 决策日志 */
    private val _decisionLogs = MutableStateFlow<List<NpcDecisionLog>>(emptyList())
    val decisionLogs: StateFlow<List<NpcDecisionLog>> = _decisionLogs.asStateFlow()

    /** 消息/交互日志 */
    private val _messageLog = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messageLog: StateFlow<List<ChatMessage>> = _messageLog.asStateFlow()

    // ========== 初始化 ==========

    init {
        // 加载配置
        viewModelScope.launch {
            settingsManager.apiConfigFlow.collect { config ->
                _apiConfig.value = config
                if (config.isValid) {
                    app.updateLlmClient(config)
                }
            }
        }

        // 检查是否有存档
        viewModelScope.launch {
            val hasSave = repository.hasSave()
            _currentScreen.value = if (hasSave) Screen.MAIN else Screen.NEW_GAME
        }
    }

    // ========== 游戏操作 ==========

    /**
     * 开始新游戏
     */
    fun startNewGame(playerName: String) {
        viewModelScope.launch {
            val result = worldEngine.startGame(playerName)
            result.onSuccess { state ->
                _messageLog.value = listOf(
                    ChatMessage(
                        text = "【天命启程】\n" +
                                "你，$playerName，一个名不见经传的修行者，出现在了清风镇的街头。\n" +
                                "远处的青云山上剑气纵横，天机城的商队从你面前经过，\n" +
                                "一个声音在你心中响起：\n" +
                                "——「这天下，你来做主。」",
                        isSystem = true
                    )
                )
                worldEngine.startTickLoop()
                tokenTracker.refreshStats()
                _currentScreen.value = Screen.MAIN
            }.onFailure { e ->
                _errorMessage.value = "启动游戏失败: ${e.message}"
            }
        }
    }

    /**
     * 载入存档
     */
    fun loadGame() {
        viewModelScope.launch {
            val result = worldEngine.startGame("")
            result.onSuccess {
                worldEngine.startTickLoop()
                _currentScreen.value = Screen.MAIN
            }
        }
    }

    /**
     * 保存游戏
     */
    fun saveGame() {
        viewModelScope.launch {
            worldEngine.saveGame()
            _messageLog.value += ChatMessage(text = "✅ 游戏已保存（第${worldEngine.worldState.value?.gameDay}天）", isSystem = true)
        }
    }

    /**
     * 前往某个地点
     */
    fun travelTo(location: String) {
        viewModelScope.launch {
            val state = worldEngine.worldState.value ?: return@launch
            state.player.location = location

            // 到达新地点时，检查该地点的NPC
            val localNpcs = state.npcs.values.filter {
                it.location == location || it.faction == getFactionByLocation(location)
            }

            val npcNames = localNpcs.take(5).joinToString("、") { it.name }
            _messageLog.value += ChatMessage(
                text = "🚶 你来到了 $location\n" +
                        "这里${if (npcNames.isNotBlank()) "有 $npcNames 等人" else "似乎没什么人"}。",
                isSystem = true
            )

            // 触发附近NPC的决策（被玩家看到）
            if (localNpcs.isNotEmpty()) {
                val targetNpc = localNpcs.first()
                val event = worldEngine.triggerNpcDecision(targetNpc.id)
                if (event != null) {
                    _messageLog.value += ChatMessage(
                        text = "📢 ${event.title}：${event.description}",
                        isSystem = true
                    )
                }
            }
        }
    }

    /**
     * 查看NPC详情
     */
    fun selectNpc(npcId: String) {
        val npc = worldEngine.worldState.value?.npcs?.get(npcId)
        _selectedNpc.value = npc
        _currentScreen.value = Screen.NPC_DETAIL
    }

    /**
     * 查看宗门详情
     */
    fun selectFaction(factionId: String) {
        val faction = worldEngine.worldState.value?.factions?.get(factionId)
        _selectedFaction.value = faction
        _currentScreen.value = Screen.FACTION_DETAIL
    }

    /**
     * 与 NPC 交互（简单的文字交互）
     */
    fun interactWithNpc(npcId: String) {
        viewModelScope.launch {
            val npc = worldEngine.worldState.value?.npcs?.get(npcId) ?: return@launch

            // 触发该NPC决策
            val event = worldEngine.triggerNpcDecision(npcId)

            _messageLog.value += ChatMessage(
                text = "💬 你与 ${npc.name} 交谈了一番。\n" +
                        "${npc.name}目前${npc.currentAction}。\n" +
                        if (event != null) "🗨️ ${event.description}" else "",
                isSystem = true
            )
        }
    }

    /**
     * 查看事件详情
     */
    fun viewEvent(eventId: String) {
        worldEngine.markEventsRead(listOf(eventId))
        val event = worldEngine.worldState.value?.eventLog?.find { it.id == eventId }
        if (event != null) {
            _messageLog.value += ChatMessage(
                text = "📜 【${event.title}】\n${event.description}\n" +
                        "时间: 第${event.gameDay}天 · 地点: ${event.location}",
                isSystem = true
            )
        }
    }

    /**
     * 标记所有事件已读
     */
    fun markAllEventsRead() {
        val ids = worldEngine.getUnreadEvents().map { it.id }
        worldEngine.markEventsRead(ids)
    }

    // ========== 设置操作 ==========

    /**
     * 更新 API 配置并测试连接
     */
    fun updateApiConfig(config: ApiConfig) {
        viewModelScope.launch {
            _apiConfig.value = config
            settingsManager.saveApiConfig(config)
            app.updateLlmClient(config)

            // 测试连接
            val testResult = app.llmClient.testConnection()
            testResult.onSuccess {
                _messageLog.value += ChatMessage(text = "✅ API 连接成功: $it", isSystem = true)
            }.onFailure {
                _errorMessage.value = "API 连接失败: ${it.message}"
            }
        }
    }

    /**
     * 更新 Token 预估
     */
    fun updateEstimation(modelName: String, mode: TokenEstimator.GameMode) {
        val est = TokenEstimator.quickEstimateByMode(modelName, mode)
        _estimation.value = est
    }

    /**
     * 获取可用模型列表（调用用户 API 的 /models 端点）
     */
    fun fetchAvailableModels() {
        viewModelScope.launch {
            _isLoadingModels.value = true
            val config = _apiConfig.value
            if (!config.isValid) {
                _isLoadingModels.value = false
                return@launch
            }
            val client = LlmClient(config)
            val result = client.fetchModels()
            result.onSuccess { models ->
                _availableModels.value = models
                // 如果当前模型不在列表中，自动选第一个
                if (models.isNotEmpty() && models.none { it == _apiConfig.value.modelName }) {
                    val newConfig = _apiConfig.value.copy(modelName = models.first())
                    _apiConfig.value = newConfig
                }
            }.onFailure { e ->
                _errorMessage.value = "获取模型列表失败: ${e.message}"
            }
            _isLoadingModels.value = false
        }
    }

    /**
     * 强制触发所有核心 NPC 决策一次（调试/验证用）
     */
    fun triggerAllNpcsDecision() {
        viewModelScope.launch {
            val state = worldEngine.worldState.value ?: return@launch
            val logs = mutableListOf<NpcDecisionLog>()

            for ((_, npc) in state.npcs) {
                if (!npc.isCoreAgent || !npc.isAlive) continue
                val event = worldEngine.triggerNpcDecision(npc.id)
                logs.add(NpcDecisionLog(
                    npcId = npc.id,
                    npcName = npc.name,
                    title = npc.title,
                    realm = npc.realm.displayName,
                    action = event?.title ?: npc.currentAction,
                    description = event?.description ?: "暂无特别行动",
                    location = npc.location
                ))
            }

            _decisionLogs.value = logs
            _messageLog.value += ChatMessage(
                text = "🧠 已触发 ${logs.size} 个核心NPC决策，查看「Agent监控」了解结果",
                isSystem = true
            )
        }
    }

    /**
     * 获取单个 NPC 的决策日志
     */
    fun refreshDecisionLogs() {
        viewModelScope.launch {
            val state = worldEngine.worldState.value ?: return@launch
            val logs = state.npcs.values
                .filter { it.isCoreAgent }
                .map { npc ->
                    NpcDecisionLog(
                        npcId = npc.id,
                        npcName = npc.name,
                        title = npc.title,
                        realm = npc.realm.displayName,
                        action = npc.currentAction,
                        description = "上次决策: 第${npc.lastDecisionDay}天 · 累计${npc.totalDecisions}次",
                        location = npc.location
                    )
                }
            _decisionLogs.value = logs
        }
    }

    /**
     * 更新游戏速度
     */
    fun updateTimeScale(scale: Float) {
        viewModelScope.launch {
            settingsManager.saveGameSettings(scale, 2_000_000, "slow_down")
            worldEngine.worldState.value?.timeScale = scale
        }
    }

    // ========== 导航 ==========

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun clearError() {
        _errorMessage.value = null
    }

    // ========== 辅助 ==========

    private fun getFactionByLocation(location: String): String? {
        val state = worldEngine.worldState.value ?: return null
        return state.factions.values.find { it.realm == location }?.id
    }

    fun getNpcsByFaction(factionId: String): List<NpcProfile> {
        val state = worldEngine.worldState.value ?: return emptyList()
        return state.npcs.values.filter { it.faction == factionId }.take(10)
    }

    fun getNpcsByLocation(location: String): List<NpcProfile> {
        val state = worldEngine.worldState.value ?: return emptyList()
        return state.npcs.values.filter { it.location == location }.take(10)
    }

    fun getAllNpcs(): List<NpcProfile> {
        return worldEngine.worldState.value?.npcs?.values?.toList() ?: emptyList()
    }

    fun getAllFactions(): List<Faction> {
        return worldEngine.worldState.value?.factions?.values?.toList() ?: emptyList()
    }
}

// ========== UI 模型 ==========

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isSystem: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * NPC 决策日志 —— 用于 Agent 监控面板
 */
data class NpcDecisionLog(
    val npcId: String,
    val npcName: String,
    val title: String,
    val realm: String,
    val action: String,
    val description: String,
    val location: String
)

enum class Screen {
    LOADING,
    NEW_GAME,
    MAIN,
    MAP,
    NPC_LIST,
    NPC_DETAIL,
    FACTION_DETAIL,
    EVENT_LOG,
    SETTINGS,
    API_SETTINGS,
    TOKEN_STATS,
    SAVE_MANAGER,
    AGENT_MONITOR
}

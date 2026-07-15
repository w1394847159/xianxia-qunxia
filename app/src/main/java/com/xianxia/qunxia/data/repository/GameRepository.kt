package com.xianxia.qunxia.data.repository

import android.content.Context
import com.google.gson.Gson
import com.xianxia.qunxia.data.db.AppDatabase
import com.xianxia.qunxia.data.db.AppSettingsEntity
import com.xianxia.qunxia.data.db.TokenLogEntity
import com.xianxia.qunxia.data.db.WorldSaveEntity
import com.xianxia.qunxia.game.engine.GameEvent
import com.xianxia.qunxia.game.faction.Faction
import com.xianxia.qunxia.game.npc.NpcProfile
import com.xianxia.qunxia.game.world.PlayerInfo
import com.xianxia.qunxia.game.world.WorldState

/**
 * 数据仓库 —— 游戏逻辑与持久化之间的桥梁
 */
class GameRepository(private val context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val dao = db.gameDao()
    private val gson = Gson()

    // ========== 世界存档 ==========

    suspend fun loadWorld(): WorldState? {
        val entity = dao.getWorldSave() ?: return null
        return try {
            gson.fromJson(entity.worldStateJson, WorldState::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun saveWorld(state: WorldState) {
        val json = gson.toJson(state)
        val entity = WorldSaveEntity(
            worldStateJson = json,
            saveTime = System.currentTimeMillis(),
            gameDay = state.gameDay,
            description = "第${state.gameDay}天存档"
        )
        dao.saveWorld(entity)
    }

    suspend fun hasSave(): Boolean {
        return dao.getWorldSave() != null
    }

    // ========== Token 日志 ==========

    suspend fun logToken(
        npcId: String, model: String,
        promptTokens: Int, completionTokens: Int,
        decisionType: String, costUsd: Double
    ) {
        dao.insertTokenLog(TokenLogEntity(
            timestamp = System.currentTimeMillis(),
            npcId = npcId,
            model = model,
            promptTokens = promptTokens,
            completionTokens = completionTokens,
            totalTokens = promptTokens + completionTokens,
            costUsd = costUsd,
            decisionType = decisionType
        ))
    }

    suspend fun getRecentTokenLogs(limit: Int = 100) =
        dao.getRecentTokenLogs(limit)

    suspend fun getTodayTokenTotal(): Long {
        val midnight = getTodayStartMillis()
        return dao.getTokenTotalSince(midnight) ?: 0L
    }

    suspend fun getWeekTokenTotal(): Long {
        val weekAgo = System.currentTimeMillis() - 7 * 24 * 3600 * 1000L
        return dao.getTokenTotalSince(weekAgo) ?: 0L
    }

    suspend fun getMonthTokenTotal(): Long {
        val monthAgo = System.currentTimeMillis() - 30L * 24 * 3600 * 1000
        return dao.getTokenTotalSince(monthAgo) ?: 0L
    }

    // ========== 设置 ==========

    suspend fun loadSettings(): AppSettingsEntity {
        return dao.getSettings() ?: AppSettingsEntity()
    }

    suspend fun saveSettings(settings: AppSettingsEntity) {
        dao.saveSettings(settings)
    }

    // ========== 新游戏初始化 ==========

    suspend fun createNewGame(playerName: String): WorldState {
        val state = WorldState(
            gameDay = 1,
            gameHour = 8,  // 辰时，清晨
            player = PlayerInfo(name = playerName, location = "清风镇")
        )
        // 初始化世界数据
        initializeWorld(state)
        saveWorld(state)
        return state
    }

    private fun initializeWorld(state: WorldState) {
        // NPC 和宗门数据从 JSON 配置加载
        // 这里先用内置的默认数据
        state.npcs.putAll(DefaultWorldData.createDefaultNpcs())
        state.factions.putAll(DefaultWorldData.createDefaultFactions())

        // 统计核心NPC数量
        state.coreAgentCount = state.npcs.values.count { it.isCoreAgent }

        // 初始化宗门关系
        DefaultWorldData.initializeDiplomacy(state.factions)
    }

    private fun getTodayStartMillis(): Long {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}

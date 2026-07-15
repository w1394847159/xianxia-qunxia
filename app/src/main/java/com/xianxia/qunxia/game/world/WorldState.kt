package com.xianxia.qunxia.game.world

import com.xianxia.qunxia.game.engine.GameEvent
import com.xianxia.qunxia.game.faction.Faction
import com.xianxia.qunxia.game.npc.NpcProfile

/**
 * 世界状态 —— 整个游戏的快照
 */
data class WorldState(
    // 时间
    var gameDay: Int = 0,           // 从第1天开始
    var gameHour: Int = 0,          // 0~23（一个时辰=2小时，简化用小时）
    var totalTicks: Long = 0,

    // 时间流速配置
    var timeScale: Float = 10f,     // 现实1分钟=游戏10分钟(默认)
    var tickIntervalMs: Long = 6000L, // 每6秒一个tick

    // 玩家
    val player: PlayerInfo = PlayerInfo(),

    // 世界内容
    val npcs: MutableMap<String, NpcProfile> = mutableMapOf(),
    val factions: MutableMap<String, Faction> = mutableMapOf(),
    val eventLog: MutableList<GameEvent> = mutableListOf(),

    // 设置
    var coreAgentCount: Int = 0       // 核心NPC数量
)

data class PlayerInfo(
    var name: String = "无名",
    var realm: Int = 0,             // 对应 Realm.level
    var location: String = "新手村",
    var health: Int = 100,
    var spirit: Int = 100,          // 精力
    var relationships: MutableMap<String, Int> = mutableMapOf(),     // npcId -> affinity
    var knownEvents: MutableList<String> = mutableListOf(),          // 已知事件ID
    var inventory: MutableList<String> = mutableListOf(),            // 背包
    var achievements: MutableList<String> = mutableListOf(),         // 成就
    var playTimeMinutes: Long = 0   // 游玩时间（现实分钟）
)

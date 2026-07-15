package com.xianxia.qunxia.game.engine

/**
 * 游戏中发生的事件
 */
data class GameEvent(
    val id: String,
    val gameDay: Int,                       // 发生的游戏日
    val gameHour: Int,                      // 发生的游戏时辰
    val type: EventType,
    val title: String,                      // 事件标题
    val description: String,                // 事件描述
    val participants: List<String> = emptyList(),  // 参与者NPC ID
    val factionIds: List<String> = emptyList(),    // 相关宗门ID
    val location: String = "未知",           // 发生地点
    val importance: Int = 1,                // 1~5，重要性
    val effects: Map<String, Any> = emptyMap(),    // 对世界的影响

    // 是否已被主角看到
    var isRead: Boolean = false
)

enum class EventType(val displayName: String) {
    NPC_BREAKTHROUGH("突破"),
    NPC_COMBAT("战斗"),
    NPC_ADVENTURE("奇遇"),
    NPC_TRAVEL("游历"),
    NPC_SOCIAL("社交"),
    NPC_DEATH("陨落"),

    FACTION_WAR("宗门战争"),
    FACTION_ALLIANCE("结盟"),
    FACTION_BETRAYAL("背叛"),
    FACTION_FEAST("宗门大典"),
    FACTION_DECLINE("衰落"),

    WORLD_SECRET("秘境开启"),
    WORLD_DISASTER("天灾"),
    WORLD_TREASURE("天材地宝"),

    PLAYER_INTERACTION("交互")
}

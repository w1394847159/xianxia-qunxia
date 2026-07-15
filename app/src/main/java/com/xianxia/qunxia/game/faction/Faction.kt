package com.xianxia.qunxia.game.faction

/**
 * 宗门
 */
data class Faction(
    val id: String,
    val name: String,
    val description: String,
    val realm: String,                     // 所在区域
    val leaderId: String,                  // 掌门 NPC ID
    val elders: List<String> = emptyList(), // 长老 ID 列表
    val disciples: List<String> = emptyList(),

    // 宗门实力
    var influence: Int = 50,               // 影响力 0~1000
    var wealth: Int = 50,                  // 资源
    var military: Int = 50,                // 战力
    var reputation: Int = 50,              // 名声

    // 外交
    var relations: MutableMap<String, DiplomaticRelation> = mutableMapOf(),

    // 状态
    var isActive: Boolean = true,
    var activeEvents: MutableList<String> = mutableListOf(),
    var foundingYear: Int = 0,             // 成立年份（游戏内）
    var tags: List<String> = emptyList()   // 标签：正派/邪道/中立/剑修/丹修/...
)

/**
 * 宗门外交关系
 */
data class DiplomaticRelation(
    val targetId: String,
    var type: DiplomaticType = DiplomaticType.NEUTRAL,
    var value: Int = 0                    // -100~100
)

enum class DiplomaticType(val displayName: String) {
    ALLY("同盟"),
    FRIENDLY("友好"),
    NEUTRAL("中立"),
    COLD("冷淡"),
    HOSTILE("敌对"),
    WAR("战争"),
    SUBORDINATE("附属"),
    DOMINANT("宗主");
}

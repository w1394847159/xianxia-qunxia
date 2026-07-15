package com.xianxia.qunxia.game

/**
 * NPC之间的关系
 */
data class Relation(
    val targetId: String,
    var affinity: Int = 0,      // -100~100 好感度
    var type: RelationType = RelationType.STRANGER,
    var lastInteraction: Long = 0,  // 游戏内时间
    var history: List<String> = emptyList() // 关系事件摘要
)

enum class RelationType(val displayName: String) {
    STRANGER("陌路人"),
    ACQUAINTANCE("相识"),
    FRIEND("朋友"),
    CLOSE_FRIEND("挚友"),
    DISCIPLE("师徒"),
    MASTER("师父"),
    FAMILY("血亲"),
    SWORN("结义兄弟"),
    LOVER("道侣"),
    RIVAL("竞争对手"),
    ENEMY("仇敌"),
    DEADLY_ENEMY("死敌"),
    SUBORDINATE("下属"),
    SUPERIOR("上级");
}

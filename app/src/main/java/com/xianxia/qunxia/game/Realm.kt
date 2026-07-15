package com.xianxia.qunxia.game

/**
 * 修仙境界体系
 */
enum class Realm(
    val displayName: String,
    val level: Int,       // 等级（数值比较用）
    val lifespanBonus: Int // 寿命加成（年）
) {
    MORTAL("凡人", 0, 0),
    LIANQI("炼气期", 1, 50),
    ZHUJI("筑基期", 2, 150),
    JINDAN("金丹期", 3, 500),
    YUANYING("元婴期", 4, 1500),
    HUASHEN("化神期", 5, 5000),
    DACHENG("大乘期", 6, 20000),
    DUJIE("渡劫期", 7, 50000),
    FEISHENG("飞升", 8, Int.MAX_VALUE);

    companion object {
        fun fromLevel(level: Int): Realm =
            entries.firstOrNull { it.level == level } ?: MORTAL
    }
}

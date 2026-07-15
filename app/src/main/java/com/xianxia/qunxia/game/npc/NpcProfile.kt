package com.xianxia.qunxia.game.npc

import com.xianxia.qunxia.game.Realm

/**
 * NPC 核心档案 —— 每个 NPC 的完整定义
 */
data class NpcProfile(
    val id: String,
    val name: String,
    val title: String,                  // 头衔，如"青云门掌门"
    val gender: String,
    val age: Int,
    var realm: Realm,
    val faction: String?,               // 所属宗门ID
    val location: String,               // 当前位置

    // 人设
    val personality: String,            // 性格描述，用于LLM system prompt
    val appearance: String,             // 外貌描述
    val background: String,             // 背景故事（简略）
    val goals: List<String>,            // 当前目标
    val quirks: List<String>,           // 怪癖/习惯
    val skills: List<String>,           // 特长

    // 是否为LLM驱动的核心NPC
    val isCoreAgent: Boolean,

    // LLM相关
    val decisionFrequency: Int = 1,     // 每几天决策一次
    var lastDecisionDay: Int = 0,       // 上次决策的游戏日

    // 状态
    var currentAction: String = "修炼",  // 当前正在做的事
    var destination: String? = null,    // 正在前往
    var isAlive: Boolean = true,
    var health: Int = 100,              // 0~100

    // 统计
    var totalDecisions: Int = 0,
    var totalTokenConsumed: Long = 0
)

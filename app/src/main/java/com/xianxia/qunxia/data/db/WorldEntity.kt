package com.xianxia.qunxia.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity: 世界状态存档
 * 只存一份"当前存档"，序列化为 JSON 字符串
 */
@Entity(tableName = "world_save")
data class WorldSaveEntity(
    @PrimaryKey val id: Int = 1,    // 固定只有一条记录
    val worldStateJson: String,      // 整个 WorldState 的 JSON
    val saveTime: Long,              // 现实时间戳
    val gameDay: Int,                // 游戏天数（方便显示）
    val description: String = ""     // 存档描述
)

/**
 * Room Entity: Token 消耗日志
 */
@Entity(tableName = "token_log")
data class TokenLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,             // 现实时间戳
    val npcId: String,
    val model: String,
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int,
    val costUsd: Double,             // 估算费用
    val decisionType: String         // 决策类型
)

/**
 * Room Entity: 设置
 */
@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val apiBaseUrl: String = "https://api.openai.com/v1",
    val apiKey: String = "",
    val modelName: String = "gpt-4o-mini",
    val maxTokensPerDecision: Int = 2048,
    val requestTimeoutSec: Int = 30,
    val concurrency: Int = 3,

    val timeScale: Float = 10f,
    val monthlyTokenBudget: Long = 2_000_000L,
    val overBudgetAction: String = "slow_down",  // slow_down |路人模式 | pause

    val playerName: String = "无名",
    val isNewGame: Boolean = true
)

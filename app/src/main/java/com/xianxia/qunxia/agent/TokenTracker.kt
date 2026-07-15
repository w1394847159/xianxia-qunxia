package com.xianxia.qunxia.agent

import com.xianxia.qunxia.data.repository.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Token 追踪器 —— 记录和统计 LLM API 消耗
 */
class TokenTracker(private val repository: GameRepository) {

    data class TokenStats(
        val todayTokens: Long = 0,
        val weekTokens: Long = 0,
        val monthTokens: Long = 0,
        val totalDecisions: Int = 0,
        val todayDecisions: Int = 0,
        val estimatedMonthlyTokens: Long = 0,
        val averagePerDecision: Int = 0
    )

    private val _stats = MutableStateFlow(TokenStats())
    val stats: StateFlow<TokenStats> = _stats.asStateFlow()

    /**
     * 记录一次决策的 token 消耗
     */
    suspend fun logDecision(
        npcId: String,
        promptTokens: Int,
        completionTokens: Int,
        model: String,
        decisionType: String
    ) {
        val totalTokens = promptTokens + completionTokens

        // 按 gpt-4o-mini 价格估算（$0.15/M input, $0.6/M output）
        val cost = (promptTokens * 0.15 + completionTokens * 0.6) / 1_000_000.0

        repository.logToken(
            npcId = npcId,
            model = model,
            promptTokens = promptTokens,
            completionTokens = completionTokens,
            decisionType = decisionType,
            costUsd = cost
        )

        refreshStats()
    }

    /**
     * 刷新统计
     */
    suspend fun refreshStats() {
        val today = repository.getTodayTokenTotal()
        val week = repository.getWeekTokenTotal()
        val month = repository.getMonthTokenTotal()

        // 计算平均每次决策消耗
        val recentLogs = repository.getRecentTokenLogs(50)
        val avgPerDecision = if (recentLogs.isNotEmpty()) {
            recentLogs.map { it.totalTokens }.average().toInt()
        } else {
            0
        }

        _stats.value = TokenStats(
            todayTokens = today,
            weekTokens = week,
            monthTokens = month,
            totalDecisions = recentLogs.size,
            todayDecisions = recentLogs.count {
                it.timestamp > System.currentTimeMillis() - 24 * 3600 * 1000
            },
            estimatedMonthlyTokens = estimateMonthly(week, avgPerDecision),
            averagePerDecision = avgPerDecision
        )
    }

    /**
     * 估算每月消耗
     */
    private fun estimateMonthly(weekTokens: Long, avgPerDecision: Int): Long {
        // 如果有周数据，用周数据推算
        if (weekTokens > 0) return weekTokens * 4
        // 否则用默认预估
        return (avgPerDecision * 30 * 20).toLong()  // 20次决策/天 * 30天
    }

    /**
     * 获取 Token 预估报告
     */
    fun getEstimationReport(): String {
        val stats = _stats.value
        return buildString {
            appendLine("📊 Token 消耗报告")
            appendLine("━━━━━━━━━━━━━━━━")
            appendLine("今日消耗: ${stats.todayTokens} token")
            appendLine("本周消耗: ${stats.weekTokens} token")
            appendLine("本月消耗: ${stats.monthTokens} token")
            appendLine("预估月消耗: ${stats.estimatedMonthlyTokens} token")
            appendLine("平均每次决策: ${stats.averagePerDecision} token")
            appendLine("━━━━━━━━━━━━━━━━")
            appendLine("按 gpt-4o-mini 估算:")
            appendLine("  月费 ≈ \$%.2f".format(stats.estimatedMonthlyTokens * 0.225 / 1_000_000))
            appendLine("按 DeepSeek 估算:")
            appendLine("  月费 ≈ ¥%.2f".format(stats.estimatedMonthlyTokens * 0.5 / 1_000_000))
        }
    }
}

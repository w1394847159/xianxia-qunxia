package com.xianxia.qunxia.agent

/**
 * Token 预估器 —— 在用户设置 API 时提供费用预估
 *
 * 根据用户选择的模型和游戏设置，预估每月 token 消耗和费用
 */
object TokenEstimator {

    data class ModelInfo(
        val name: String,
        val displayName: String,
        val pricePerInput: Double,   // $/1M tokens
        val pricePerOutput: Double,  // $/1M tokens
        val provider: String
    )

    // 常见模型价格表（用户可查阅）
    val knownModels = listOf(
        ModelInfo("gpt-4o-mini", "GPT-4o Mini", 0.15, 0.60, "OpenAI"),
        ModelInfo("gpt-4o", "GPT-4o", 2.50, 10.00, "OpenAI"),
        ModelInfo("gpt-4-turbo", "GPT-4 Turbo", 10.00, 30.00, "OpenAI"),
        ModelInfo("deepseek-chat", "DeepSeek V3", 0.27, 1.10, "DeepSeek"),
        ModelInfo("deepseek-reasoner", "DeepSeek R1", 0.55, 2.19, "DeepSeek"),
        ModelInfo("glm-4-plus", "GLM-4 Plus", 0.50, 0.50, "智谱AI"),
        ModelInfo("qwen-plus", "Qwen Plus", 0.80, 2.00, "阿里云"),
        ModelInfo("qwen-turbo", "Qwen Turbo", 0.30, 0.60, "阿里云"),
        ModelInfo("moonshot-v1", "Moonshot v1", 1.00, 2.00, "月之暗面"),
        ModelInfo("claude-sonnet-4", "Claude Sonnet 4", 3.00, 15.00, "Anthropic")
    )

    /**
     * 预估结果
     */
    data class Estimation(
        val dailyDecisions: Int,
        val dailyTokens: Long,
        val monthlyTokens: Long,
        val monthlyCostUsd: Double,
        val monthlyCostCny: Double,      // 约合人民币
        val perDecisionTokens: Int,
        val breakdown: String
    )

    /**
     * 根据设置预估消耗
     *
     * @param modelName 模型名称
     * @param coreNpcCount 核心NPC数量
     * @param decisionsPerDay 每个NPC每天决策次数
     * @param tokensPerDecision 每次决策预估消耗
     */
    fun estimate(
        modelName: String,
        coreNpcCount: Int = 20,
        decisionsPerDay: Int = 1,
        tokensPerDecision: Int = 1800
    ): Estimation {
        val dailyDecisions = coreNpcCount * decisionsPerDay
        val dailyTokens = dailyDecisions.toLong() * tokensPerDecision
        val monthlyTokens = dailyTokens * 30

        // 查找模型价格
        val model = knownModels.find { it.name == modelName }
        val inputPrice = model?.pricePerInput ?: 0.50  // 默认0.5
        val outputPrice = model?.pricePerOutput ?: 1.0

        // 按 70% input / 30% output 估算
        val monthlyInput = monthlyTokens * 0.7
        val monthlyOutput = monthlyTokens * 0.3
        val monthlyCost = (monthlyInput * inputPrice + monthlyOutput * outputPrice) / 1_000_000

        val breakdown = buildString {
            appendLine("核心NPC: $coreNpcCount 个")
            appendLine("每日总决策: $dailyDecisions 次")
            appendLine("每次估算: $tokensPerDecision token")
            appendLine("---")
            appendLine("每日消耗: $dailyTokens token")
            appendLine("月消耗: $monthlyTokens token")
            appendLine("---")
            appendLine("模型: ${model?.displayName ?: modelName}")
            appendLine("输入价格: \$$inputPrice/M")
            appendLine("输出价格: \$$outputPrice/M")
            appendLine("月费 ≈ \$${"%.2f".format(monthlyCost)}")
            appendLine(" ≈ ¥${"%.2f".format(monthlyCost * 7.3)}")
        }

        return Estimation(
            dailyDecisions = dailyDecisions,
            dailyTokens = dailyTokens,
            monthlyTokens = monthlyTokens,
            monthlyCostUsd = monthlyCost,
            monthlyCostCny = monthlyCost * 7.3,
            perDecisionTokens = tokensPerDecision,
            breakdown = breakdown
        )
    }

    /**
     * 根据游戏模式快速预估
     */
    fun quickEstimateByMode(modelName: String, mode: GameMode): Estimation {
        return when (mode) {
            GameMode.LEISURELY -> estimate(modelName, 10, 2, 1500)
            GameMode.STANDARD -> estimate(modelName, 20, 1, 1800)
            GameMode.LIVELY -> estimate(modelName, 30, 1, 2000)
            GameMode.EPIC -> estimate(modelName, 40, 1.5, 2200)
        }
    }

    enum class GameMode(val displayName: String, val description: String) {
        LEISURELY("悠闲", "10核心NPC，每日2次决策，适合慢慢品味"),
        STANDARD("标准", "20核心NPC，每日1次决策，默认推荐"),
        LIVELY("热闹", "30核心NPC，每日1次决策，江湖风云变幻"),
        EPIC("风云", "40核心NPC，每日1~2次决策，波云诡谲")
    }
}

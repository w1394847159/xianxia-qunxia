package com.xianxia.qunxia.settings

/**
 * LLM API 配置 —— 用户自行配置
 */
data class ApiConfig(
    val baseUrl: String = "https://api.openai.com/v1",
    val apiKey: String = "",
    val modelName: String = "gpt-4o-mini",
    val maxTokensPerDecision: Int = 2048,
    val requestTimeoutSec: Int = 30,
    val concurrency: Int = 3
) {
    val isValid: Boolean
        get() = apiKey.isNotBlank() && baseUrl.isNotBlank()
}

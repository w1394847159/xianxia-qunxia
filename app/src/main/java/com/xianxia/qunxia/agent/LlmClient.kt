package com.xianxia.qunxia.agent

import android.util.Log
import com.google.gson.Gson
import com.xianxia.qunxia.settings.ApiConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * LLM API 客户端 —— 直接与用户配置的 API 交互
 *
 * 支持 OpenAI 兼容接口（OpenAI / 硅基流动 / DeepSeek / 任意）
 */
class LlmClient(private val apiConfig: ApiConfig) {
    companion object {
        private const val TAG = "LlmClient"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(apiConfig.requestTimeoutSec.toLong(), java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(apiConfig.requestTimeoutSec.toLong(), java.util.concurrent.TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor())
        .build()

    private val gson = Gson()

    /**
     * LLM 调用结果
     */
    data class ChatResult(
        val content: String,
        val promptTokens: Int,
        val completionTokens: Int,
        val model: String
    )

    /**
     * 发起聊天请求
     */
    suspend fun chat(
        systemPrompt: String,
        userMessage: String,
        maxTokens: Int = 2048,
        temperature: Double = 0.7,
        model: String = apiConfig.modelName
    ): ChatResult = withContext(Dispatchers.IO) {
        val url = "${apiConfig.baseUrl}/chat/completions"

        val requestBody = buildJsonRequestBody(systemPrompt, userMessage, model, maxTokens, temperature)

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer ${apiConfig.apiKey}")
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: throw Exception("Empty response body")

        if (!response.isSuccessful) {
            throw Exception("API error ${response.code}: $responseBody")
        }

        parseChatResponse(responseBody, model)
    }

    /**
     * 发送批量请求（用于同时让多个 NPC 做决策）
     */
    suspend fun chatBatch(
        messages: List<Pair<String, String>>,  // (systemPrompt, userMessage) 列表
        maxTokens: Int = 1024,
        temperature: Double = 0.8
    ): List<ChatResult> = withContext(Dispatchers.IO) {
        messages.map { (sys, user) ->
            try {
                chat(sys, user, maxTokens, temperature)
            } catch (e: Exception) {
                Log.w(TAG, "批量请求中单条失败", e)
                ChatResult("", 0, 0, apiConfig.modelName)
            }
        }
    }

    /**
     * 测试 API 连通性
     */
    suspend fun testConnection(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val url = "${apiConfig.baseUrl}/models"
            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer ${apiConfig.apiKey}")
                .get()
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            if (response.isSuccessful) {
                Result.success("连接成功: ${response.code}")
            } else {
                Result.failure(Exception("API 返回错误: ${response.code} - $body"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== 内部方法 ==========

    private fun buildJsonRequestBody(
        systemPrompt: String, userMessage: String,
        model: String, maxTokens: Int, temperature: Double
    ): RequestBody {
        val jsonObj = mapOf(
            "model" to model,
            "messages" to listOf(
                mapOf("role" to "system", "content" to systemPrompt),
                mapOf("role" to "user", "content" to userMessage)
            ),
            "max_tokens" to maxTokens,
            "temperature" to temperature,
            "response_format" to mapOf("type" to "json_object")
        )
        val json = gson.toJson(jsonObj)
        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        return json.toRequestBody(mediaType)
    }

    private fun parseChatResponse(responseBody: String, model: String): ChatResult {
        val root = gson.fromJson(responseBody, Map::class.java)
        if (root !is Map<*, *>) return ChatResult("", 0, 0, model)

        val choicesRaw = root["choices"]
        val firstChoice = if (choicesRaw is List<*>) choicesRaw.firstOrNull() else null
        val messageRaw = if (firstChoice is Map<*, *>) firstChoice["message"] else null
        val content = if (messageRaw is Map<*, *>) {
            (messageRaw["content"] as? String) ?: ""
        } else ""

        val usageRaw = root["usage"]
        val usage = if (usageRaw is Map<*, *>) usageRaw else null
        val promptTokens = (usage?.get("prompt_tokens") as? Number)?.toInt() ?: 0
        val completionTokens = (usage?.get("completion_tokens") as? Number)?.toInt() ?: 0

        return ChatResult(
            content = content,
            promptTokens = promptTokens,
            completionTokens = completionTokens,
            model = model
        )
    }
}

/**
 * OkHttp 日志拦截器（debug用）
 */
class HttpLoggingInterceptor : Interceptor {
    private val tag = "LlmClient"

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        Log.d(tag, "→ ${request.method} ${request.url}")
        val response = chain.proceed(request)
        Log.d(tag, "← ${response.code} ${response.request.url}")
        return response
    }
}

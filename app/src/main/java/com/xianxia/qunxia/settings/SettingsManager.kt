package com.xianxia.qunxia.settings

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "xianxia_settings")

/**
 * 设置管理器 —— 所有用户配置的读写
 */
class SettingsManager(private val context: Context) {

    private object Keys {
        val API_BASE_URL = stringPreferencesKey("api_base_url")
        val API_KEY = stringPreferencesKey("api_key")
        val MODEL_NAME = stringPreferencesKey("model_name")
        val MAX_TOKENS_PER_DECISION = intPreferencesKey("max_tokens_per_decision")
        val REQUEST_TIMEOUT = intPreferencesKey("request_timeout")

        val TIME_SCALE = floatPreferencesKey("time_scale")
        val MONTHLY_BUDGET = longPreferencesKey("monthly_token_budget")
        val OVER_BUDGET_ACTION = stringPreferencesKey("over_budget_action")

        val PLAYER_NAME = stringPreferencesKey("player_name")
        val IS_NEW_GAME = booleanPreferencesKey("is_new_game")
    }

    // ========== API 配置 ==========

    val apiConfigFlow: Flow<ApiConfig> = context.dataStore.data.map { prefs ->
        ApiConfig(
            baseUrl = prefs[Keys.API_BASE_URL] ?: "https://api.openai.com/v1",
            apiKey = prefs[Keys.API_KEY] ?: "",
            modelName = prefs[Keys.MODEL_NAME] ?: "gpt-4o-mini",
            maxTokensPerDecision = prefs[Keys.MAX_TOKENS_PER_DECISION] ?: 2048,
            requestTimeoutSec = prefs[Keys.REQUEST_TIMEOUT] ?: 30,
            concurrency = 3
        )
    }

    suspend fun saveApiConfig(config: ApiConfig) {
        context.dataStore.edit { prefs ->
            prefs[Keys.API_BASE_URL] = config.baseUrl
            prefs[Keys.API_KEY] = config.apiKey
            prefs[Keys.MODEL_NAME] = config.modelName
            prefs[Keys.MAX_TOKENS_PER_DECISION] = config.maxTokensPerDecision
            prefs[Keys.REQUEST_TIMEOUT] = config.requestTimeoutSec
        }
    }

    // ========== 游戏设置 ==========

    val timeScaleFlow: Flow<Float> = context.dataStore.data.map {
        it[Keys.TIME_SCALE] ?: 10f
    }

    val monthlyBudgetFlow: Flow<Long> = context.dataStore.data.map {
        it[Keys.MONTHLY_BUDGET] ?: 2_000_000L
    }

    val overBudgetActionFlow: Flow<String> = context.dataStore.data.map {
        it[Keys.OVER_BUDGET_ACTION] ?: "slow_down"
    }

    suspend fun saveGameSettings(
        timeScale: Float,
        monthlyBudget: Long,
        overBudgetAction: String
    ) {
        context.dataStore.edit { prefs ->
            prefs[Keys.TIME_SCALE] = timeScale
            prefs[Keys.MONTHLY_BUDGET] = monthlyBudget
            prefs[Keys.OVER_BUDGET_ACTION] = overBudgetAction
        }
    }

    // ========== 玩家信息 ==========

    val playerNameFlow: Flow<String> = context.dataStore.data.map {
        it[Keys.PLAYER_NAME] ?: "无名"
    }

    val isNewGameFlow: Flow<Boolean> = context.dataStore.data.map {
        it[Keys.IS_NEW_GAME] ?: true
    }

    suspend fun setPlayerName(name: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.PLAYER_NAME] = name
        }
    }

    suspend fun setNewGameStarted() {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_NEW_GAME] = false
        }
    }

    /**
     * 读取完整的 API 配置（同步方式，用于初始化）
     */
    suspend fun getApiConfigSync(): ApiConfig {
        val prefs = context.dataStore.data.first()
        return ApiConfig(
            baseUrl = prefs[Keys.API_BASE_URL] ?: "https://api.openai.com/v1",
            apiKey = prefs[Keys.API_KEY] ?: "",
            modelName = prefs[Keys.MODEL_NAME] ?: "gpt-4o-mini",
            maxTokensPerDecision = prefs[Keys.MAX_TOKENS_PER_DECISION] ?: 2048,
            requestTimeoutSec = prefs[Keys.REQUEST_TIMEOUT] ?: 30
        )
    }
}

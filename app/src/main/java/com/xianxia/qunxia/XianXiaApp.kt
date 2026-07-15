package com.xianxia.qunxia

import android.app.Application
import com.xianxia.qunxia.agent.LlmClient
import com.xianxia.qunxia.agent.TokenEstimator
import com.xianxia.qunxia.agent.TokenTracker
import com.xianxia.qunxia.data.repository.GameRepository
import com.xianxia.qunxia.game.engine.NpcDecisionMaker
import com.xianxia.qunxia.game.engine.WorldEngine
import com.xianxia.qunxia.settings.ApiConfig
import com.xianxia.qunxia.settings.SettingsManager

/**
 * 修仙群侠传 Application
 */
class XianXiaApp : Application() {

    lateinit var settingsManager: SettingsManager
    lateinit var repository: GameRepository
    lateinit var llmClient: LlmClient
    lateinit var tokenTracker: TokenTracker
    lateinit var npcDecisionMaker: NpcDecisionMaker
    lateinit var worldEngine: WorldEngine
    lateinit var tokenEstimator: TokenEstimator

    private val _isInitialized = mutableSetOf<String>()

    override fun onCreate() {
        super.onCreate()
        instance = this

        // 初始化无依赖的组件
        settingsManager = SettingsManager(this)
        repository = GameRepository(this)
        tokenTracker = TokenTracker(repository)
        tokenEstimator = TokenEstimator

        // LLM Client 需要 API 配置，暂时用默认值
        // 用户配置后会重新初始化
        llmClient = LlmClient(ApiConfig())
        npcDecisionMaker = NpcDecisionMaker(llmClient, tokenTracker)
        worldEngine = WorldEngine(repository, npcDecisionMaker)
    }

    /**
     * 更新 LLM Client（当用户修改 API 配置后调用）
     */
    fun updateLlmClient(config: ApiConfig) {
        llmClient = LlmClient(config)
        npcDecisionMaker = NpcDecisionMaker(llmClient, tokenTracker)
        worldEngine = WorldEngine(repository, npcDecisionMaker)
    }

    override fun onTerminate() {
        worldEngine.shutdown()
        super.onTerminate()
    }

    companion object {
        lateinit var instance: XianXiaApp
            private set
    }
}

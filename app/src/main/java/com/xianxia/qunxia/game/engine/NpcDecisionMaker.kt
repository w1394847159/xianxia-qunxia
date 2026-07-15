package com.xianxia.qunxia.game.engine

import android.util.Log
import com.xianxia.qunxia.agent.LlmClient
import com.xianxia.qunxia.agent.TokenTracker
import com.xianxia.qunxia.game.npc.NpcProfile
import com.xianxia.qunxia.game.world.WorldState

/**
 * NPC 决策器 —— 用 LLM 驱动核心 NPC 做出决策
 *
 * 每个核心 NPC 被调用时：
 * 1. 构建 system prompt（包含角色人设、世界观）
 * 2. 构建 context（当前状态、近期事件）
 * 3. 调用 LLM API → 获取结构化决策
 * 4. 解析结果并更新 NPC 状态
 */
class NpcDecisionMaker(
    private val llmClient: LlmClient,
    private val tokenTracker: TokenTracker
) {
    companion object {
        private const val TAG = "NpcDecisionMaker"
    }

    /**
     * 让一个 NPC 做出决策，返回由此产生的事件（可能为 null）
     */
    suspend fun makeDecision(npc: NpcProfile, state: WorldState): GameEvent? {
        return try {
            // 1. 构建 Prompt
            val systemPrompt = buildSystemPrompt(npc, state)
            val userPrompt = buildUserPrompt(npc, state)

            // 2. 调用 LLM
            val result = llmClient.chat(
                systemPrompt = systemPrompt,
                userMessage = userPrompt,
                maxTokens = 1024,
                temperature = 0.8
            )

            // 3. 记录 token
            tokenTracker.logDecision(
                npcId = npc.id,
                promptTokens = result.promptTokens,
                completionTokens = result.completionTokens,
                model = result.model,
                decisionType = "daily_decision"
            )

            // 4. 解析决策
            val decision = parseDecision(result.content, npc, state)

            // 5. 更新 NPC 状态
            applyDecision(npc, state, decision)

            // 6. 生成事件
            if (decision.description.isNotBlank()) {
                GameEvent(
                    id = "evt_${npc.id}_${state.gameDay}_${state.gameHour}",
                    gameDay = state.gameDay,
                    gameHour = state.gameHour,
                    type = mapDecisionToEventType(decision.action),
                    title = "${npc.name}：${decision.actionDescription}",
                    description = decision.description,
                    participants = listOf(npc.id),
                    location = decision.target ?: npc.location,
                    importance = calculateImportance(decision)
                )
            } else null

        } catch (e: Exception) {
            Log.w(TAG, "NPC ${npc.name} 决策失败: ${e.message}")
            // 降级：使用默认行为
            handleFallbackDecision(npc)
        }
    }

    // ========== Prompt 构建 ==========

    private fun buildSystemPrompt(npc: NpcProfile, state: WorldState): String {
        return """
你是一个修仙世界的角色，以下是你的身份设定。请以角色的身份做决策，严格按照 JSON 格式输出。

【你的身份】
姓名：${npc.name}
头衔：${npc.title}
性别：${npc.gender}
年龄：${npc.age}
修为：${npc.realm.displayName}
所属：${npc.faction ?: "散修"}
性格：${npc.personality}
外貌：${npc.appearance}
背景：${npc.background}
当前目标：${npc.goals.joinToString("、")}
怪癖：${npc.quirks.joinToString("、")}
特长：${npc.skills.joinToString("、")}

【当前时间】
第 ${state.gameDay} 天，${state.gameHour} 时
${state.player.name} 目前在 ${state.player.location}

【输出格式】
你必须只输出一个 JSON 对象，不要有任何其他内容：
{
    "thought": "你的内心独白（20~50字），在想什么",
    "action": "move_to | train | socialize | explore | rest | seek_treasure | fight | breakthrough | special",
    "action_description": "用一句话描述你要做什么",
    "target": "目标地点或人物ID（可为null）",
    "description": "如果这件事值得被世人知道，描述它（30~80字）。如果没有值得一提的事，返回空字符串",
    "realm_change": null 或 "突破到X期"
}
""".trimIndent()
    }

    private fun buildUserPrompt(npc: NpcProfile, state: WorldState): String {
        // 整理NPC当前状态和近期相关事件
        val recentEvents = state.eventLog
            .filter { it.participants.contains(npc.id) || it.factionIds.contains(npc.faction) }
            .takeLast(5)
            .joinToString("\n") { "  - [第${it.gameDay}天] ${it.title}: ${it.description}" }

        return """
现在是第 ${state.gameDay} 天，你正在 ${npc.location}。

你当前在做的事：${npc.currentAction}
${if (npc.destination != null) "你正在前往：${npc.destination}" else ""}

最近与你相关的事件：
${if (recentEvents.isNotBlank()) recentEvents else "  - 暂无特别事件"}

【问题】
作为 $npc.name，你今天打算做什么？请用 JSON 格式输出你的决策。
""".trimIndent()
    }

    // ========== 决策解析 ==========

    private fun parseDecision(jsonStr: String, npc: NpcProfile, state: WorldState): DecisionResult {
        return try {
            // 尝试从 JSON 中提取关键字段
            val json = jsonStr.trim()
            // 简单的 JSON 解析（生产环境建议用 Gson）
            val thought = extractJsonField(json, "thought") ?: ""
            val action = extractJsonField(json, "action") ?: "train"
            val actionDesc = extractJsonField(json, "action_description") ?: "继续修炼"
            val target = extractJsonField(json, "target")
            val desc = extractJsonField(json, "description") ?: ""

            DecisionResult(
                thought = thought,
                action = action,
                actionDescription = actionDesc,
                target = target,
                description = desc
            )
        } catch (e: Exception) {
            Log.w(TAG, "解析决策JSON失败: ${e.message}")
            DecisionResult(action = "train", actionDescription = "继续修炼")
        }
    }

    private fun applyDecision(npc: NpcProfile, state: WorldState, decision: DecisionResult) {
        npc.currentAction = decision.actionDescription

        when (decision.action) {
            "move_to" -> npc.destination = decision.target
            "breakthrough" -> {
                // 突破
                val currentLevel = npc.realm.level
                if (currentLevel < 8) {
                    val newLevel = (currentLevel + 1).coerceAtMost(8)
                    npc.realm = com.xianxia.qunxia.game.Realm.fromLevel(newLevel)
                }
            }
            "fight" -> {
                // 战斗可能导致受伤
                if (Math.random() < 0.3) {
                    npc.health = (npc.health - (10..30).random()).coerceAtLeast(0)
                }
            }
        }
    }

    private fun handleFallbackDecision(npc: NpcProfile): GameEvent? {
        npc.currentAction = listOf("修炼", "打坐", "四处走走", "翻阅典籍").random()
        return null  // LLM 失败时不生成事件
    }

    private fun mapDecisionToEventType(action: String): EventType {
        return when (action) {
            "move_to" -> EventType.NPC_TRAVEL
            "train" -> EventType.NPC_BREAKTHROUGH
            "socialize" -> EventType.NPC_SOCIAL
            "explore" -> EventType.NPC_ADVENTURE
            "seek_treasure" -> EventType.NPC_ADVENTURE
            "fight" -> EventType.NPC_COMBAT
            "breakthrough" -> EventType.NPC_BREAKTHROUGH
            else -> EventType.NPC_SOCIAL
        }
    }

    private fun calculateImportance(decision: DecisionResult): Int {
        return when (decision.action) {
            "breakthrough" -> 4
            "fight" -> 3
            "seek_treasure" -> 3
            "special" -> 4
            else -> 2
        }
    }

    /**
     * 简易 JSON 字段提取（正则）
     */
    private fun extractJsonField(json: String, field: String): String? {
        val regex = """"$field"\s*:\s*"((?:[^"\\]|\\.)*)"""".toRegex()
        val match = regex.find(json) ?: return null
        return match.groupValues[1]
    }

    private data class DecisionResult(
        val thought: String = "",
        val action: String = "train",
        val actionDescription: String = "继续修炼",
        val target: String? = null,
        val description: String = ""
    )
}

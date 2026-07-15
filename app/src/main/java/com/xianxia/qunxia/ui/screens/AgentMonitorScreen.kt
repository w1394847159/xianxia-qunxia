package com.xianxia.qunxia.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xianxia.qunxia.MainViewModel
import com.xianxia.qunxia.NpcDecisionLog
import com.xianxia.qunxia.Screen

/**
 * Agent 监控面板 —— 让玩家看到每个 NPC 的决策状态
 *
 * 这是「多 Agent 是否真的在工作」的实锤证据。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentMonitorScreen(viewModel: MainViewModel) {
    val decisionLogs by viewModel.decisionLogs.collectAsState()
    val worldState by viewModel.worldState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🧠 Agent 监控") },
                navigationIcon = {
                    TextButton(onClick = { viewModel.navigateTo(Screen.SETTINGS) }) {
                        Text("← 返回")
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.refreshDecisionLogs() }) {
                        Text("刷新")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // 状态总览
            worldState?.let { state ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "Agent 运行状态",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        val totalNpcs = state.npcs.size
                        val coreNpcs = state.npcs.values.count { it.isCoreAgent }
                        val aliveNpcs = state.npcs.values.count { it.isAlive }
                        val totalDecisions = state.npcs.values.sumOf { it.totalDecisions }

                        Text("总 NPC: $totalNpcs | 核心 Agent: $coreNpcs | 存活: $aliveNpcs")
                        Text("累计决策: $totalDecisions 次 | 当前游戏日: 第${state.gameDay}天")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.triggerAllNpcsDecision() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("🔥 强制所有Agent决策")
                }

                OutlinedButton(
                    onClick = { viewModel.refreshDecisionLogs() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("🔄 刷新状态")
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                "💡 点击「强制所有Agent决策」会立即调用LLM让每个核心NPC做一次决策。需要已配置有效的API。",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Agent 列表
            Text(
                "核心 Agent（${decisionLogs.size} 个）",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (decisionLogs.isEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "暂无数据，点击「刷新状态」或「强制决策」",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(decisionLogs) { log ->
                        AgentCard(log, viewModel)
                    }
                }
            }
        }
    }
}

@Composable
private fun AgentCard(log: NpcDecisionLog, viewModel: MainViewModel) {
    Card(
        onClick = { viewModel.selectNpc(log.npcId) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "${log.npcName} · ${log.title}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        log.realm,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        "Agent",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider()
            Spacer(modifier = Modifier.height(4.dp))

            // 当前行动
            Row {
                Text(
                    "当前行动：",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    log.action,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            // 描述
            Text(
                log.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 2
            )

            // 位置
            Text(
                "📍 ${log.location}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

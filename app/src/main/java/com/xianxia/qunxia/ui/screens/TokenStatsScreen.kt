package com.xianxia.qunxia.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xianxia.qunxia.MainViewModel
import com.xianxia.qunxia.Screen

/**
 * Token 统计与预估仪表盘
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TokenStatsScreen(viewModel: MainViewModel) {
    val stats by viewModel.tokenStats.collectAsState()
    val apiConfig by viewModel.apiConfig.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📊 Token 统计") },
                navigationIcon = {
                    TextButton(onClick = { viewModel.navigateTo(Screen.SETTINGS) }) {
                        Text("← 返回")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 概览卡片
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Token 消耗概览", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem("今日", "${stats.todayTokens}")
                            StatItem("本周", "${stats.weekTokens}")
                            StatItem("本月", "${stats.monthTokens}")
                        }

                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem("总决策", "${stats.totalDecisions}")
                            StatItem("今日决策", "${stats.todayDecisions}")
                            StatItem("均次消耗", "${stats.averagePerDecision}")
                        }
                    }
                }
            }

            // 预估卡片
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("📈 月度预估", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))

                        val monthlyTokens = if (stats.monthTokens > 0) stats.monthTokens else stats.estimatedMonthlyTokens

                        Text("预估月消耗: $monthlyTokens token")
                        Spacer(modifier = Modifier.height(4.dp))

                        // 价格估算
                        val modelName = apiConfig.modelName
                        val costGptMini = monthlyTokens * 0.225 / 1_000_000.0
                        val costDeepSeek = monthlyTokens * 0.5 / 1_000_000.0

                        Text(
                            "💰 费用估算 (当前模型: $modelName)",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            "  按 GPT-4o Mini: 约 \$${"%.2f".format(costGptMini)}/月",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "  按 DeepSeek:   约 \$%.2f/月".format(costDeepSeek),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "  按 $0.15/M input / $0.6/M output 均价估算",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            // 预算设置
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("🎯 预算控制", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))

                        var budget by remember { mutableStateOf("2000000") }
                        OutlinedTextField(
                            value = budget,
                            onValueChange = { budget = it },
                            label = { Text("月预算上限 (token)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        var overAction by remember { mutableStateOf("slow_down") }
                        Text("超预算后：")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = overAction == "slow_down",
                                onClick = { overAction = "slow_down" },
                                label = { Text("降频") }
                            )
                            FilterChip(
                                selected = overAction == "路人模式",
                                onClick = { overAction = "路人模式" },
                                label = { Text("停止LLM") }
                            )
                            FilterChip(
                                selected = overAction == "pause",
                                onClick = { overAction = "pause" },
                                label = { Text("暂停演化") }
                            )
                        }
                    }
                }
            }

            // 最近Token消耗日志
            item {
                Text(
                    "最近 Token 消耗记录",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // 这里可以从 repository 加载最近的 token 日志显示
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "⚠️ API 配置正确后，这里会显示每次 AI 决策的 Token 消耗明细",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
    }
}

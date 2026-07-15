package com.xianxia.qunxia.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.xianxia.qunxia.MainViewModel
import com.xianxia.qunxia.Screen

/**
 * 设置主界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("⚙️ 设置") },
                navigationIcon = {
                    TextButton(onClick = { viewModel.navigateTo(Screen.MAIN) }) {
                        Text("← 返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // API 设置
            Card(
                onClick = { viewModel.navigateTo(Screen.API_SETTINGS) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("🤖 LLM API 配置", style = MaterialTheme.typography.titleSmall)
                        val config by viewModel.apiConfig.collectAsState()
                        Text(
                            if (config.isValid) "✅ 已配置 · ${config.modelName}"
                            else "⚠️ 未配置 · 核心NPC将不会行动",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    Text("→")
                }
            }

            // Token 统计
            Card(
                onClick = { viewModel.navigateTo(Screen.TOKEN_STATS) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("📊 Token 统计与预估", style = MaterialTheme.typography.titleSmall)
                        val stats by viewModel.tokenStats.collectAsState()
                        Text(
                            "今日 ${stats.todayTokens} · 本周 ${stats.weekTokens}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    Text("→")
                }
            }

            // 游戏速度
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("⏱️ 时间流速", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))

                    var scale by remember { mutableFloatStateOf(10f) }
                    Text("现实1分钟 = 游戏${scale.toInt()}分钟")
                    Slider(
                        value = scale,
                        onValueChange = { scale = it },
                        valueRange = 1f..60f,
                        steps = 11
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("慢", style = MaterialTheme.typography.labelSmall)
                        Text("快", style = MaterialTheme.typography.labelSmall)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.updateTimeScale(scale) }, modifier = Modifier.fillMaxWidth()) {
                        Text("应用")
                    }
                }
            }

            // 存档管理
            Card(
                onClick = { viewModel.navigateTo(Screen.SAVE_MANAGER) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("💾 存档管理", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "保存/读取/导出存档",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    Text("→")
                }
            }

            // 关于
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("📖 关于", style = MaterialTheme.typography.titleSmall)
                    Text("修仙群侠传 v0.1.0", style = MaterialTheme.typography.bodySmall)
                    Text(
                        "一个由多 AI Agent 驱动的活修仙世界。\n每个核心NPC都有自己的想法和目标。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

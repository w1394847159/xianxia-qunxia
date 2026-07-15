package com.xianxia.qunxia.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xianxia.qunxia.MainViewModel
import com.xianxia.qunxia.Screen

/**
 * NPC 详情页 —— 查看人物档案与交互
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NpcDetailScreen(viewModel: MainViewModel) {
    val npc by viewModel.selectedNpc.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(npc?.name ?: "人物详情") },
                navigationIcon = {
                    TextButton(onClick = { viewModel.navigateTo(Screen.NPC_LIST) }) {
                        Text("← 返回")
                    }
                }
            )
        }
    ) { padding ->
        if (npc == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                Text("未选择人物", modifier = Modifier.padding(16.dp))
            }
            return@Scaffold
        }

        val p = npc!!

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // 基本信息卡片
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(p.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        if (p.isCoreAgent) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    "AI 驱动",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))

                    DetailRow("头衔", p.title)
                    DetailRow("性别", p.gender)
                    DetailRow("年龄", "${p.age}岁")
                    DetailRow("修为", p.realm.displayName)
                    DetailRow("宗门", p.faction ?: "散修")
                    DetailRow("位置", p.location)
                    DetailRow("当前", p.currentAction)

                    if (!p.isAlive) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "【已陨落】",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 性格与背景
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("性格", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(p.personality, style = MaterialTheme.typography.bodyMedium)

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("背景", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(p.background, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 目标与特长
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("当前目标", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    p.goals.forEach { goal ->
                        Text("• $goal", style = MaterialTheme.typography.bodyMedium)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("特长", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(p.skills.joinToString(" · "), style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 交互按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { viewModel.interactWithNpc(p.id) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("💬 交谈")
                }
                OutlinedButton(
                    onClick = { viewModel.travelTo(p.location) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("🚶 前往")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Token 消耗统计（仅核心NPC）
            if (p.isCoreAgent) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "AI 统计",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text("累计决策: ${p.totalDecisions} 次")
                        Text("累计消耗: ${p.totalTokenConsumed} token")
                        Text("决策频率: 每${p.decisionFrequency}天一次")
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Text(
            "$label：",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.width(60.dp)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

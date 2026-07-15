package com.xianxia.qunxia.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.xianxia.qunxia.MainViewModel
import com.xianxia.qunxia.Screen

/**
 * 存档管理
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveManagerScreen(viewModel: MainViewModel) {
    val worldState by viewModel.worldState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("💾 存档管理") },
                navigationIcon = {
                    TextButton(onClick = { viewModel.navigateTo(Screen.SETTINGS) }) {
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 当前游戏状态
            worldState?.let { state ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("当前进度", style = MaterialTheme.typography.titleSmall)
                        Text("第 ${state.gameDay} 天 · ${state.player.name}")
                        Text("位置: ${state.player.location}")
                        Text("NPC 存活: ${state.npcs.values.count { it.isAlive }}/${state.npcs.size}")
                    }
                }
            }

            // 操作按钮
            Button(
                onClick = { viewModel.saveGame() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("💾 保存当前进度")
            }

            OutlinedButton(
                onClick = { /* 导出存档：将WorldState序列化为JSON文件 */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("📤 导出存档 (分享/备份)")
            }

            OutlinedButton(
                onClick = { /* 导入存档：从JSON文件恢复 */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("📥 导入存档")
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // 存档列表
            Text("自动存档记录", style = MaterialTheme.typography.titleSmall)

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "⚠️ 当前为单存档模式，每次保存覆盖上一个存档。" +
                                "后续版本将支持多存档位和自动存档历史。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            // 危险操作
            Spacer(modifier = Modifier.height(16.dp))
            Text("危险操作", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.error)

            OutlinedButton(
                onClick = { /* 确认后重新开始 */ },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🗑️ 重新开始 (删除所有进度)")
            }
        }
    }
}

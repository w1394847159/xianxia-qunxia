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
import com.xianxia.qunxia.ChatMessage
import com.xianxia.qunxia.MainViewModel
import com.xianxia.qunxia.Screen
import com.xianxia.qunxia.game.engine.GameEvent
import com.xianxia.qunxia.game.world.WorldState

/**
 * 主游戏界面 —— 中央事件流 + 状态栏 + 底部导航
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val worldState by viewModel.worldState.collectAsState()
    val events by viewModel.recentEvents.collectAsState()
    val messages by viewModel.messageLog.collectAsState()
    val isRunning by viewModel.isGameRunning.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    worldState?.let { state ->
                        Column {
                            Text("修仙群侠传 · 第${state.gameDay}天", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "${state.player.location} · ${state.player.name}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.saveGame() }) {
                        Text("💾", style = MaterialTheme.typography.titleMedium)
                    }
                    IconButton(onClick = { viewModel.navigateTo(Screen.SETTINGS) }) {
                        Text("⚙️", style = MaterialTheme.typography.titleMedium)
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Text("🏯") },
                    label = { Text("江湖") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { viewModel.navigateTo(Screen.MAP) },
                    icon = { Text("🗺️") },
                    label = { Text("地图") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { viewModel.navigateTo(Screen.NPC_LIST) },
                    icon = { Text("👥") },
                    label = { Text("人物") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { viewModel.navigateTo(Screen.EVENT_LOG) },
                    icon = { Text("📜") },
                    label = { Text("事件") }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // 系统消息/交互历史
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages.reversed()) { msg ->
                    MessageBubble(msg)
                }

                // 最近事件
                if (messages.isEmpty()) {
                    items(events.take(10)) { event ->
                        EventCard(event, viewModel)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 快捷操作栏
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                worldState?.let { state ->
                    val factions = viewModel.getAllFactions()
                    factions.take(4).forEach { faction ->
                        AssistChip(
                            onClick = { viewModel.selectFaction(faction.id) },
                            label = { Text(faction.name, style = MaterialTheme.typography.bodySmall) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 指令输入行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { viewModel.navigateTo(Screen.MAP) }) {
                    Text("🚶 前往...")
                }
                TextButton(onClick = { viewModel.navigateTo(Screen.NPC_LIST) }) {
                    Text("💬 拜访...")
                }
                TextButton(onClick = { viewModel.saveGame() }) {
                    Text("📖 打坐存档")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun MessageBubble(msg: ChatMessage) {
    Surface(
        color = if (msg.isSystem)
            MaterialTheme.colorScheme.surface
        else
            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = msg.text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(12.dp),
            color = if (msg.isSystem)
                MaterialTheme.colorScheme.onSurface
            else
                MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun EventCard(event: GameEvent, viewModel: MainViewModel) {
    val importanceColors = mapOf(
        1 to MaterialTheme.colorScheme.outline,
        2 to MaterialTheme.colorScheme.secondary,
        3 to MaterialTheme.colorScheme.primary,
        4 to MaterialTheme.colorScheme.tertiary,
        5 to MaterialTheme.colorScheme.error
    )
    val color = importanceColors[event.importance] ?: MaterialTheme.colorScheme.outline

    Card(
        onClick = { viewModel.viewEvent(event.id) },
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (event.isRead)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 重要性色条
            Surface(
                modifier = Modifier
                    .width(4.dp)
                    .height(40.dp),
                color = color,
                shape = MaterialTheme.shapes.small
            ) {}

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (!event.isRead) FontWeight.Bold else FontWeight.Normal
                )
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = "第${event.gameDay}天 · ${event.location}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

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
import com.xianxia.qunxia.game.engine.EventType

/**
 * 事件日志 —— 江湖大事记
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventLogScreen(viewModel: MainViewModel) {
    val worldState by viewModel.worldState.collectAsState()
    val events = worldState?.eventLog ?: emptyList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📜 江湖大事记") },
                navigationIcon = {
                    TextButton(onClick = { viewModel.navigateTo(Screen.MAIN) }) {
                        Text("← 返回")
                    }
                },
                actions = {
                    if (events.any { !it.isRead }) {
                        TextButton(onClick = { viewModel.markAllEventsRead() }) {
                            Text("全部已读")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (events.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("江湖上还没有值得记载的大事...", style = MaterialTheme.typography.bodyLarge)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(events.reversed()) { event ->
                Card(
                    onClick = { viewModel.viewEvent(event.id) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (event.isRead)
                            MaterialTheme.colorScheme.surface
                        else
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                event.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (!event.isRead) FontWeight.Bold else FontWeight.Normal
                            )
                            Text(
                                event.type.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        Text(
                            event.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            maxLines = 3
                        )
                        Text(
                            "第${event.gameDay}天 · ${event.location} · 重要性 ${"⭐".repeat(event.importance)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}

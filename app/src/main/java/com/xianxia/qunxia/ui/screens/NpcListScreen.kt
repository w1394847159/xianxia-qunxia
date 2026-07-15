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
 * 人物列表 —— 查看所有 NPC
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NpcListScreen(viewModel: MainViewModel) {
    val npcs = viewModel.getAllNpcs()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("👥 江湖人物") },
                navigationIcon = {
                    TextButton(onClick = { viewModel.navigateTo(Screen.MAIN) }) {
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 按宗门分组
            val factions = viewModel.getAllFactions()
            for (faction in factions) {
                val factionNpcs = npcs.filter { it.faction == faction.id }
                if (factionNpcs.isNotEmpty()) {
                    item {
                        Text(
                            faction.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                        )
                    }
                    items(factionNpcs) { npc ->
                        Card(
                            onClick = { viewModel.selectNpc(npc.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "${npc.name} · ${npc.title}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        "${npc.realm.displayName} · ${npc.currentAction}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                                if (npc.isCoreAgent) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                        shape = MaterialTheme.shapes.small
                                    ) {
                                        Text(
                                            "核心",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 散修
            val sanxiuNpcs = npcs.filter { it.faction == null }
            if (sanxiuNpcs.isNotEmpty()) {
                item {
                    Text(
                        "散修",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                    )
                }
                items(sanxiuNpcs) { npc ->
                    Card(
                        onClick = { viewModel.selectNpc(npc.id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp)) {
                            Column {
                                Text("${npc.name} · ${npc.title}", style = MaterialTheme.typography.titleSmall)
                                Text(
                                    "${npc.realm.displayName} · ${npc.location}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

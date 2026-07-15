package com.xianxia.qunxia.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.xianxia.qunxia.MainViewModel
import com.xianxia.qunxia.Screen

/**
 * 地图界面 —— 宗门分布与地点导航
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(viewModel: MainViewModel) {
    val worldState by viewModel.worldState.collectAsState()
    val factions = viewModel.getAllFactions()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🗺️ 江湖地图") },
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 玩家当前位置
            item {
                worldState?.let { state ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("📍 当前位置", style = MaterialTheme.typography.titleSmall)
                            Text(
                                "${state.player.location}",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "第${state.gameDay}天 · ${state.gameHour}时",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            // 可前往的地点
            item {
                Text("可前往的宗门与地点", style = MaterialTheme.typography.titleMedium)
            }

            // 宗门列表（可点击前往）
            items(factions) { faction ->
                val isCurrentLocation = worldState?.player?.location == faction.realm
                Card(
                    onClick = {
                        if (!isCurrentLocation) viewModel.travelTo(faction.realm)
                    },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCurrentLocation)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        else
                            MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(faction.name, style = MaterialTheme.typography.titleSmall)
                            Text(
                                faction.realm,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                "影响力 ${faction.influence} · 战力 ${faction.military}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        if (isCurrentLocation) {
                            Text("📍", style = MaterialTheme.typography.titleLarge)
                        } else {
                            Text("🚶", style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
            }

            // 其他地点
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text("其他地点", style = MaterialTheme.typography.titleMedium)
            }

            val otherLocations = listOf(
                "清风镇" to "新手聚集的小镇",
                "药王谷" to "隐世丹师药老人的居所",
                "苗疆十万大山" to "蓝凤凰的地盘"
            )
            items(otherLocations) { (name, desc) ->
                val isHere = worldState?.player?.location == name
                Card(
                    onClick = { if (!isHere) viewModel.travelTo(name) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(name, style = MaterialTheme.typography.titleSmall)
                            Text(desc, style = MaterialTheme.typography.bodySmall)
                        }
                        if (isHere) Text("📍") else Text("🚶")
                    }
                }
            }
        }
    }
}

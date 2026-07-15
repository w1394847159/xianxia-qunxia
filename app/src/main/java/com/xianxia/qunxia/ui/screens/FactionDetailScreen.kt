package com.xianxia.qunxia.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xianxia.qunxia.MainViewModel
import com.xianxia.qunxia.Screen
import com.xianxia.qunxia.game.faction.DiplomaticType
import com.xianxia.qunxia.game.faction.Faction

/**
 * 宗门详情
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FactionDetailScreen(viewModel: MainViewModel) {
    val faction by viewModel.selectedFaction.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(faction?.name ?: "宗门详情") },
                navigationIcon = {
                    TextButton(onClick = { viewModel.navigateTo(Screen.MAP) }) {
                        Text("← 返回")
                    }
                }
            )
        }
    ) { padding ->
        if (faction == null) return@Scaffold

        val f = faction!!

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // 宗门概况
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(f.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(f.realm, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(f.description, style = MaterialTheme.typography.bodyMedium)

                    Spacer(modifier = Modifier.height(12.dp))

                    // 属性条
                    StatBar("影响力", f.influence)
                    StatBar("资源", f.wealth)
                    StatBar("战力", f.military)
                    StatBar("名声", f.reputation)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 宗门成员
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("主要成员", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                    val leader = viewModel.getAllNpcs().find { it.id == f.leaderId }
                    if (leader != null) {
                        TextButton(onClick = { viewModel.selectNpc(leader.id) }) {
                            Text("掌门：${leader.name} · ${leader.realm.displayName}")
                        }
                    }

                    val elderNpcs = viewModel.getAllNpcs().filter { it.id in f.elders }
                    elderNpcs.forEach { elder ->
                        TextButton(onClick = { viewModel.selectNpc(elder.id) }) {
                            Text("长老：${elder.name} · ${elder.realm.displayName}")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 外交关系
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("外交关系", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    f.relations.forEach { (targetId, rel) ->
                        val targetName = viewModel.getAllFactions().find { it.id == targetId }?.name ?: targetId
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(targetName, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                rel.type.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = when (rel.type) {
                                    DiplomaticType.ALLY -> MaterialTheme.colorScheme.primary
                                    DiplomaticType.HOSTILE, DiplomaticType.WAR -> MaterialTheme.colorScheme.error
                                    DiplomaticType.FRIENDLY -> MaterialTheme.colorScheme.secondary
                                    else -> MaterialTheme.colorScheme.outline
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 前往
            Button(
                onClick = { viewModel.travelTo(f.realm) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🚶 前往 ${f.realm}")
            }
        }
    }
}

@Composable
private fun StatBar(label: String, value: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Text("$label：", style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(60.dp))
        LinearProgressIndicator(
            progress = { value / 1000f },
            modifier = Modifier
                .weight(1f)
                .height(6.dp),
        )
        Text(
            "$value",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

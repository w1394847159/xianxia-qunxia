package com.xianxia.qunxia.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import com.xianxia.qunxia.MainViewModel
import com.xianxia.qunxia.Screen

/**
 * App 导航入口
 */
@Composable
fun AppNavigation(viewModel: MainViewModel = viewModel()) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    when (currentScreen) {
        Screen.LOADING -> LoadingScreen()
        Screen.NEW_GAME -> NewGameScreen(viewModel)
        Screen.MAIN -> MainScreen(viewModel)
        Screen.MAP -> MapScreen(viewModel)
        Screen.NPC_LIST -> NpcListScreen(viewModel)
        Screen.NPC_DETAIL -> NpcDetailScreen(viewModel)
        Screen.FACTION_DETAIL -> FactionDetailScreen(viewModel)
        Screen.EVENT_LOG -> EventLogScreen(viewModel)
        Screen.SETTINGS -> SettingsScreen(viewModel)
        Screen.API_SETTINGS -> ApiSettingsScreen(viewModel)
        Screen.TOKEN_STATS -> TokenStatsScreen(viewModel)
        Screen.SAVE_MANAGER -> SaveManagerScreen(viewModel)
        Screen.AGENT_MONITOR -> AgentMonitorScreen(viewModel)
    }

    // 错误提示
    errorMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("错误") },
            text = { Text(msg) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("确定")
                }
            }
        )
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "修仙群侠传\n载入中...",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center
        )
    }
}

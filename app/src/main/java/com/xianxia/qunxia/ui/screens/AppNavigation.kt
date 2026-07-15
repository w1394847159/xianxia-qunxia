package com.xianxia.qunxia.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
    }

    // 错误提示
    errorMessage?.let { msg ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { androidx.compose.material3.Text("错误") },
            text = { androidx.compose.material3.Text(msg) },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { viewModel.clearError() }) {
                    androidx.compose.material3.Text("确定")
                }
            }
        )
    }
}

@Composable
fun LoadingScreen() {
    androidx.compose.foundation.layout.Box(
        modifier = androidx.compose.ui.Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.material3.Text(
            text = "修仙群侠传\n载入中...",
            style = androidx.compose.material3.MaterialTheme.typography.headlineLarge,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

private fun androidx.compose.ui.Modifier.fillMaxSize() =
    this.then(androidx.compose.foundation.layout.Modifier.fillMaxSize())

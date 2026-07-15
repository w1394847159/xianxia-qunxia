package com.xianxia.qunxia.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.xianxia.qunxia.MainViewModel

/**
 * 新游戏 —— 输入名字
 */
@Composable
fun NewGameScreen(viewModel: MainViewModel) {
    var playerName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "修仙群侠传",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "一个活的修仙世界",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "请输入你的名字",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = playerName,
            onValueChange = { if (it.length <= 8) playerName = it },
            label = { Text("道号") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(0.7f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (playerName.isNotBlank()) {
                    viewModel.startNewGame(playerName.trim())
                }
            },
            enabled = playerName.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(48.dp)
        ) {
            Text("踏入江湖", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 如果有存档，显示继续按钮
        val hasSave by remember { mutableStateOf(false) } // 实际应由 ViewModel 提供
        if (hasSave) {
            TextButton(onClick = { viewModel.loadGame() }) {
                Text("继续上次的旅程")
            }
        }
    }
}

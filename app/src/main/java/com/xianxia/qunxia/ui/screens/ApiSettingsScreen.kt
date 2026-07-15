package com.xianxia.qunxia.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.xianxia.qunxia.MainViewModel
import com.xianxia.qunxia.Screen
import com.xianxia.qunxia.agent.TokenEstimator
import com.xianxia.qunxia.settings.ApiConfig

/**
 * API 配置界面 —— 用户设置自己的 LLM API
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiSettingsScreen(viewModel: MainViewModel) {
    val currentConfig by viewModel.apiConfig.collectAsState()
    val estimation by viewModel.estimation.collectAsState()

    var baseUrl by remember { mutableStateOf(currentConfig.baseUrl) }
    var apiKey by remember { mutableStateOf(currentConfig.apiKey) }
    var modelName by remember { mutableStateOf(currentConfig.modelName) }
    var maxTokens by remember { mutableIntStateOf(currentConfig.maxTokensPerDecision) }
    var timeout by remember { mutableIntStateOf(currentConfig.requestTimeoutSec) }
    var selectedMode by remember { mutableStateOf(TokenEstimator.GameMode.STANDARD) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🤖 LLM API 配置") },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 接口地址
            Text("接口地址 (Base URL)", style = MaterialTheme.typography.labelMedium)
            OutlinedTextField(
                value = baseUrl,
                onValueChange = { baseUrl = it },
                placeholder = { Text("https://api.openai.com/v1") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // API Key
            Text("API Key", style = MaterialTheme.typography.labelMedium)
            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                placeholder = { Text("sk-...") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // 模型选择（从 API 动态获取）
            Text("模型", style = MaterialTheme.typography.labelMedium)

            // 获取模型列表按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isLoadingModels by viewModel.isLoadingModels.collectAsState()
                val availableModels by viewModel.availableModels.collectAsState()

                if (availableModels.isEmpty()) {
                    Text(
                        "点击「获取模型」从接口拉取列表",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                } else {
                    Text(
                        "共 ${availableModels.size} 个模型可用",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Button(
                    onClick = {
                        val config = ApiConfig(baseUrl, apiKey, modelName, maxTokens, timeout)
                        viewModel.updateApiConfig(config)
                        viewModel.fetchAvailableModels()
                    },
                    enabled = !isLoadingModels && baseUrl.isNotBlank() && apiKey.isNotBlank(),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        if (isLoadingModels) "加载中..." else "获取模型",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            // 模型下拉选择
            var expanded by remember { mutableStateOf(false) }
            val availableModels by viewModel.availableModels.collectAsState()
            val modelOptions = availableModels.ifEmpty { listOf(modelName) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = modelName,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    modelOptions.forEach { model ->
                        DropdownMenuItem(
                            text = { Text(model) },
                            onClick = {
                                modelName = model
                                expanded = false
                            }
                        )
                    }
                }
            }

            // 高级设置
            Text("高级设置", style = MaterialTheme.typography.titleSmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = maxTokens.toString(),
                    onValueChange = { maxTokens = it.toIntOrNull() ?: 2048 },
                    label = { Text("每次最大Token") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = timeout.toString(),
                    onValueChange = { timeout = it.toIntOrNull() ?: 30 },
                    label = { Text("超时(秒)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            // Token 预估
            Spacer(modifier = Modifier.height(8.dp))
            Text("Token 费用预估", style = MaterialTheme.typography.titleMedium)

            // 模式选择
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TokenEstimator.GameMode.entries.forEach { mode ->
                    FilterChip(
                        selected = selectedMode == mode,
                        onClick = {
                            selectedMode = mode
                            viewModel.updateEstimation(modelName, mode)
                        },
                        label = { Text(mode.displayName, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            // 预估显示
            if (estimation != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("📊 预估报告", style = MaterialTheme.typography.labelMedium)
                        Text(
                            estimation!!.breakdown,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 保存与测试按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        viewModel.updateEstimation(modelName, selectedMode)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("💰 预估费用")
                }

                Button(
                    onClick = {
                        val config = ApiConfig(
                            baseUrl = baseUrl,
                            apiKey = apiKey,
                            modelName = modelName,
                            maxTokensPerDecision = maxTokens,
                            requestTimeoutSec = timeout
                        )
                        viewModel.updateApiConfig(config)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("💾 保存并测试")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 常用 API 参考
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("📝 常用 API 参考", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        """
                        |OpenAI:    https://api.openai.com/v1
                        |DeepSeek:  https://api.deepseek.com
                        |硅基流动:  https://api.siliconflow.cn/v1
                        |智谱AI:    https://open.bigmodel.cn/api/paas/v4
                        |阿里云:    https://dashscope.aliyuncs.com/compatible-mode/v1
                        |
                        |💡 推荐使用 gpt-4o-mini 或 DeepSeek，
                        |   性价比高，月费仅几毛到几块钱。
                        """.trimMargin(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

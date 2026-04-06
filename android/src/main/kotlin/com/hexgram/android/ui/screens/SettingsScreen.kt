package com.hexgram.android.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hexgram.android.ui.components.GoldButton
import com.hexgram.android.ui.components.PanelCard
import com.hexgram.android.ui.components.SectionHeader
import com.hexgram.android.ui.theme.HexgramColors
import com.hexgram.android.ui.theme.SerifFont
import kotlinx.coroutines.launch

private const val PREFS_NAME = "hexgram_settings"
private const val KEY_ENDPOINT = "worker_endpoint"
private const val KEY_PROVIDER = "ai_provider"
private const val KEY_API_KEY = "api_key"
private const val KEY_MODEL = "ai_model"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    var endpoint by remember { mutableStateOf(prefs.getString(KEY_ENDPOINT, "") ?: "") }
    var provider by remember { mutableStateOf(prefs.getString(KEY_PROVIDER, "anthropic") ?: "anthropic") }
    var apiKey by remember { mutableStateOf(prefs.getString(KEY_API_KEY, "") ?: "") }
    var model by remember { mutableStateOf(prefs.getString(KEY_MODEL, "") ?: "") }
    var showApiKey by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val providers = listOf(
        "anthropic" to "Anthropic",
        "openai" to "OpenAI",
        "openrouter" to "OpenRouter"
    )

    val defaultModels = mapOf(
        "anthropic" to "claude-sonnet-4-20250514",
        "openai" to "gpt-4o",
        "openrouter" to "anthropic/claude-sonnet-4-20250514"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "设置",
                        fontFamily = SerifFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = HexgramColors.gold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = HexgramColors.gold
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = HexgramColors.bgPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = HexgramColors.bgPrimary
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            SectionHeader(subtitle = "SETTINGS", title = "AI 设置")

            Spacer(modifier = Modifier.height(24.dp))

            // Connection mode info
            PanelCard {
                Column {
                    Text(
                        text = "连接模式",
                        fontSize = 14.sp,
                        fontFamily = SerifFont,
                        fontWeight = FontWeight.Bold,
                        color = HexgramColors.gold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "配置Worker后端地址可使用安全的服务端代理模式；或直接填写API Key使用直连模式（开发调试用）。",
                        fontSize = 13.sp,
                        fontFamily = SerifFont,
                        color = HexgramColors.textSecondary,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Worker endpoint
            PanelCard {
                Column {
                    SettingsLabel("Worker 后端地址")
                    Spacer(modifier = Modifier.height(6.dp))
                    SettingsTextField(
                        value = endpoint,
                        onValueChange = { endpoint = it },
                        placeholder = "https://your-worker.workers.dev"
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "留空则使用直连API模式",
                        fontSize = 11.sp,
                        fontFamily = SerifFont,
                        color = HexgramColors.textTertiary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Provider selection
            PanelCard {
                Column {
                    SettingsLabel("AI 提供商")
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for ((key, label) in providers) {
                            val isSelected = provider == key
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) HexgramColors.gold.copy(alpha = 0.15f)
                                        else HexgramColors.bgResult
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) HexgramColors.gold.copy(alpha = 0.5f)
                                        else HexgramColors.border,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        provider = key
                                        if (model.isBlank() || model == defaultModels.values.find { it != defaultModels[key] }) {
                                            model = defaultModels[key] ?: ""
                                        }
                                    }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 13.sp,
                                    fontFamily = SerifFont,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) HexgramColors.gold
                                    else HexgramColors.textSecondary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // API Key
            PanelCard {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SettingsLabel("API Key")
                        Text(
                            text = if (showApiKey) "隐藏" else "显示",
                            fontSize = 12.sp,
                            fontFamily = SerifFont,
                            color = HexgramColors.gold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { showApiKey = !showApiKey }
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    SettingsTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        placeholder = "sk-...",
                        visualTransformation = if (showApiKey) VisualTransformation.None
                        else PasswordVisualTransformation()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "直连模式需填写；Worker模式可留空",
                        fontSize = 11.sp,
                        fontFamily = SerifFont,
                        color = HexgramColors.textTertiary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Model
            PanelCard {
                Column {
                    SettingsLabel("模型名称")
                    Spacer(modifier = Modifier.height(6.dp))
                    SettingsTextField(
                        value = model,
                        onValueChange = { model = it },
                        placeholder = defaultModels[provider] ?: "model name"
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "留空使用默认模型",
                        fontSize = 11.sp,
                        fontFamily = SerifFont,
                        color = HexgramColors.textTertiary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save button
            GoldButton(
                text = "保存设置",
                onClick = {
                    prefs.edit()
                        .putString(KEY_ENDPOINT, endpoint)
                        .putString(KEY_PROVIDER, provider)
                        .putString(KEY_API_KEY, apiKey)
                        .putString(KEY_MODEL, model)
                        .apply()

                    scope.launch {
                        snackbarHostState.showSnackbar("设置已保存")
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // About section
            HorizontalDivider(color = HexgramColors.border.copy(alpha = 0.3f))

            Spacer(modifier = Modifier.height(16.dp))

            PanelCard {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "易学三合",
                        fontSize = 18.sp,
                        fontFamily = SerifFont,
                        fontWeight = FontWeight.Bold,
                        color = HexgramColors.gold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Hexgram",
                        fontSize = 13.sp,
                        fontFamily = SerifFont,
                        color = HexgramColors.textTertiary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "六爻纳甲 · 四柱八字 · 黄历查询",
                        fontSize = 13.sp,
                        fontFamily = SerifFont,
                        color = HexgramColors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "传统易学 · 现代呈现",
                        fontSize = 12.sp,
                        fontFamily = SerifFont,
                        color = HexgramColors.textTertiary
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingsLabel(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        fontFamily = SerifFont,
        fontWeight = FontWeight.Medium,
        color = HexgramColors.textSecondary
    )
}

@Composable
private fun SettingsTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                placeholder,
                color = HexgramColors.textTertiary,
                fontFamily = SerifFont,
                fontSize = 14.sp
            )
        },
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = HexgramColors.textPrimary,
            unfocusedTextColor = HexgramColors.textPrimary,
            cursorColor = HexgramColors.gold,
            focusedBorderColor = HexgramColors.gold,
            unfocusedBorderColor = HexgramColors.border,
            focusedContainerColor = HexgramColors.bgResult,
            unfocusedContainerColor = HexgramColors.bgResult
        ),
        shape = RoundedCornerShape(8.dp),
        singleLine = true,
        visualTransformation = visualTransformation,
        textStyle = androidx.compose.ui.text.TextStyle(
            fontFamily = SerifFont,
            fontSize = 14.sp
        )
    )
}

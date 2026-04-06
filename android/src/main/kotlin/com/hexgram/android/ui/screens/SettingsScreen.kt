package com.hexgram.android.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    var endpoint by remember { mutableStateOf(prefs.getString(KEY_ENDPOINT, "") ?: "") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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

            // Worker endpoint
            PanelCard {
                Column {
                    Text(
                        text = "Worker 后端地址",
                        fontSize = 14.sp,
                        fontFamily = SerifFont,
                        fontWeight = FontWeight.Medium,
                        color = HexgramColors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = endpoint,
                        onValueChange = { endpoint = it },
                        placeholder = {
                            Text(
                                "https://yijing-api.workers.dev",
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
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = SerifFont,
                            fontSize = 14.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "AI提示词和API Key安全存储在服务端，App仅发送排盘数据",
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

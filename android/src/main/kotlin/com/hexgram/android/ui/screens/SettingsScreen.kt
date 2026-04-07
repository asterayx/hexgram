package com.hexgram.android.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hexgram.android.BuildConfig
import com.hexgram.android.ui.components.PanelCard
import com.hexgram.android.ui.theme.HexgramColors
import com.hexgram.android.ui.theme.SerifFont

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "关于",
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
        containerColor = HexgramColors.bgPrimary
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // App title
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "易学",
                    fontSize = 28.sp,
                    fontFamily = SerifFont,
                    fontWeight = FontWeight.Bold,
                    color = HexgramColors.gold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Hexgram",
                    fontSize = 14.sp,
                    fontFamily = SerifFont,
                    color = HexgramColors.textTertiary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "六爻纳甲 · 四柱八字 · 黄历查询 · 北帝灵签",
                    fontSize = 13.sp,
                    fontFamily = SerifFont,
                    color = HexgramColors.textSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "传统易学 · 现代呈现",
                    fontSize = 12.sp,
                    fontFamily = SerifFont,
                    color = HexgramColors.textTertiary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // About
            PanelCard {
                Column {
                    Text(
                        text = "关于",
                        fontSize = 14.sp,
                        fontFamily = SerifFont,
                        fontWeight = FontWeight.Medium,
                        color = HexgramColors.goldLight
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "集六爻纳甲排盘、四柱八字命理、传统黄历择日、北帝玄天大帝灵签四大功能于一体的专业易学应用。排盘计算在本地完成，AI深度解读由云端提供。",
                        fontSize = 12.sp,
                        fontFamily = SerifFont,
                        color = HexgramColors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Build info
            PanelCard {
                Column {
                    InfoRow("版本", BuildConfig.VERSION_NAME)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Build", fontSize = 12.sp, fontFamily = SerifFont, color = HexgramColors.textSecondary)
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = BuildConfig.GIT_HASH,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = HexgramColors.textTertiary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(label, fontSize = 12.sp, fontFamily = SerifFont, color = HexgramColors.textSecondary)
        Spacer(modifier = Modifier.weight(1f))
        Text(value, fontSize = 12.sp, fontFamily = SerifFont, color = HexgramColors.textPrimary)
    }
}

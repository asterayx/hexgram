package com.hexgram.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hexgram.android.ui.components.GhostButton
import com.hexgram.android.ui.components.InfoRow
import com.hexgram.android.ui.components.LoadingSpinner
import com.hexgram.android.ui.components.MarkdownText
import com.hexgram.android.ui.components.PanelCard
import com.hexgram.android.ui.components.ResultCard
import com.hexgram.android.ui.components.SectionHeader
import com.hexgram.android.ui.components.TagChip
import com.hexgram.android.ui.theme.HexgramColors
import com.hexgram.android.ui.theme.SerifFont
import com.hexgram.android.viewmodels.HuangliViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HuangliScreen(
    viewModel: HuangliViewModel = viewModel()
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SectionHeader(subtitle = "HUANGLI", title = "黄历查询")

        Spacer(modifier = Modifier.height(20.dp))

        // Date navigation
        PanelCard {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Navigation row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.previousDay() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "前一天",
                            tint = HexgramColors.gold,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${viewModel.selectedYear}年${viewModel.selectedMonth}月${viewModel.selectedDay}日",
                            fontSize = 20.sp,
                            fontFamily = SerifFont,
                            fontWeight = FontWeight.Bold,
                            color = HexgramColors.goldLight
                        )
                        viewModel.result?.let { result ->
                            Text(
                                text = result.ganZhiDate,
                                fontSize = 14.sp,
                                fontFamily = SerifFont,
                                color = HexgramColors.textSecondary
                            )
                        }
                    }

                    IconButton(onClick = { viewModel.nextDay() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "后一天",
                            tint = HexgramColors.gold,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quick date input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NumberPickerField(
                        label = "年",
                        value = viewModel.selectedYear,
                        onValueChange = {
                            viewModel.selectedYear = it
                            viewModel.calculate()
                        },
                        range = 1900..2100,
                        modifier = Modifier.weight(1f)
                    )
                    NumberPickerField(
                        label = "月",
                        value = viewModel.selectedMonth,
                        onValueChange = {
                            viewModel.selectedMonth = it
                            viewModel.calculate()
                        },
                        range = 1..12,
                        modifier = Modifier.weight(1f)
                    )
                    NumberPickerField(
                        label = "日",
                        value = viewModel.selectedDay,
                        onValueChange = {
                            viewModel.selectedDay = it
                            viewModel.calculate()
                        },
                        range = 1..31,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Today button
                Text(
                    text = "回到今天",
                    fontSize = 13.sp,
                    fontFamily = SerifFont,
                    color = HexgramColors.gold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { viewModel.goToToday() }
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }

        // Result display
        viewModel.result?.let { result ->
            Spacer(modifier = Modifier.height(16.dp))

            // Gan Zhi header card
            PanelCard {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Large day display
                    Text(
                        text = "${viewModel.selectedDay}",
                        fontSize = 56.sp,
                        fontFamily = SerifFont,
                        fontWeight = FontWeight.Bold,
                        color = HexgramColors.goldLight
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        InfoBadge(label = "年", value = result.nianGanZhi)
                        InfoBadge(label = "月", value = result.yueGanZhi)
                        InfoBadge(label = "日", value = result.riGanZhi)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TagChip(text = result.jianChu)
                        TagChip(text = result.xiu)
                        if (result.lunarDate.isNotBlank()) {
                            TagChip(text = result.lunarDate, color = HexgramColors.textSecondary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Yi (宜) section
            if (result.yi.isNotEmpty()) {
                ResultCard {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(HexgramColors.yiGreen.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "宜",
                                    fontSize = 16.sp,
                                    fontFamily = SerifFont,
                                    fontWeight = FontWeight.Bold,
                                    color = HexgramColors.yiGreen
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "今日宜",
                                fontSize = 14.sp,
                                fontFamily = SerifFont,
                                color = HexgramColors.textSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (item in result.yi) {
                                TagChip(text = item, color = HexgramColors.yiGreen)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Ji (忌) section
            if (result.ji.isNotEmpty()) {
                ResultCard {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(HexgramColors.jiRed.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "忌",
                                    fontSize = 16.sp,
                                    fontFamily = SerifFont,
                                    fontWeight = FontWeight.Bold,
                                    color = HexgramColors.jiRed
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "今日忌",
                                fontSize = 14.sp,
                                fontFamily = SerifFont,
                                color = HexgramColors.textSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (item in result.ji) {
                                TagChip(text = item, color = HexgramColors.jiRed)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Ji Shen Fang Wei (吉神方位)
            if (result.jishenFangwei.isNotEmpty()) {
                PanelCard {
                    Column {
                        Text(
                            text = "吉神方位",
                            fontSize = 14.sp,
                            fontFamily = SerifFont,
                            fontWeight = FontWeight.Bold,
                            color = HexgramColors.gold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        val items = result.jishenFangwei.entries.toList()
                        // Display in grid: 2 columns
                        for (i in items.indices step 2) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                FangweiItem(
                                    label = items[i].key,
                                    value = items[i].value,
                                    modifier = Modifier.weight(1f)
                                )
                                if (i + 1 < items.size) {
                                    FangweiItem(
                                        label = items[i + 1].key,
                                        value = items[i + 1].value,
                                        modifier = Modifier.weight(1f)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Detail info
            PanelCard {
                Column {
                    Text(
                        text = "详细信息",
                        fontSize = 14.sp,
                        fontFamily = SerifFont,
                        fontWeight = FontWeight.Bold,
                        color = HexgramColors.gold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    InfoRow(label = "建星", value = result.jianChu)
                    Spacer(modifier = Modifier.height(6.dp))
                    InfoRow(label = "二十八宿", value = result.xiu)
                    Spacer(modifier = Modifier.height(6.dp))
                    if (result.chongSha.isNotBlank()) {
                        InfoRow(
                            label = "冲煞",
                            value = result.chongSha,
                            valueColor = HexgramColors.jiRed
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    if (result.pengzuBaiji.isNotBlank()) {
                        HorizontalDivider(
                            color = HexgramColors.border.copy(alpha = 0.3f),
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                        Text(
                            text = "彭祖百忌",
                            fontSize = 13.sp,
                            fontFamily = SerifFont,
                            color = HexgramColors.textSecondary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = result.pengzuBaiji,
                            fontSize = 14.sp,
                            fontFamily = SerifFont,
                            color = HexgramColors.textPrimary,
                            lineHeight = 22.sp
                        )
                    }
                    if (result.jiShen.isNotEmpty()) {
                        HorizontalDivider(
                            color = HexgramColors.border.copy(alpha = 0.3f),
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                        Text(
                            text = "吉神",
                            fontSize = 13.sp,
                            fontFamily = SerifFont,
                            color = HexgramColors.textSecondary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            for (item in result.jiShen) {
                                TagChip(text = item, color = HexgramColors.yiGreen)
                            }
                        }
                    }
                    if (result.xiongSha.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "凶煞",
                            fontSize = 13.sp,
                            fontFamily = SerifFont,
                            color = HexgramColors.textSecondary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            for (item in result.xiongSha) {
                                TagChip(text = item, color = HexgramColors.jiRed)
                            }
                        }
                    }
                }
            }

            // AI section
            Spacer(modifier = Modifier.height(16.dp))

            if (viewModel.aiLoading) {
                LoadingSpinner("AI解读中...")
            } else if (viewModel.aiText.isNotBlank()) {
                ResultCard {
                    Column {
                        Text(
                            text = "🤖 AI深度解读",
                            fontSize = 16.sp,
                            fontFamily = SerifFont,
                            fontWeight = FontWeight.Bold,
                            color = HexgramColors.gold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        MarkdownText(text = viewModel.aiText)
                    }
                }
            } else {
                GhostButton(
                    text = "🤖 AI深度解读",
                    onClick = { viewModel.requestAI() }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun InfoBadge(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontFamily = SerifFont,
            color = HexgramColors.textTertiary
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontFamily = SerifFont,
            fontWeight = FontWeight.Bold,
            color = HexgramColors.textPrimary
        )
    }
}

@Composable
private fun FangweiItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(HexgramColors.gold.copy(alpha = 0.6f))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            fontFamily = SerifFont,
            color = HexgramColors.textSecondary,
            modifier = Modifier.width(48.dp)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontFamily = SerifFont,
            color = HexgramColors.textPrimary,
            fontWeight = FontWeight.Medium
        )
    }
}

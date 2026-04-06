package com.hexgram.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hexgram.android.ui.components.GhostButton
import com.hexgram.android.ui.components.GoldButton
import com.hexgram.android.ui.components.InfoRow
import com.hexgram.android.ui.components.LoadingSpinner
import com.hexgram.android.ui.components.MarkdownText
import com.hexgram.android.ui.components.PanelCard
import com.hexgram.android.ui.components.ResultCard
import com.hexgram.android.ui.components.SectionHeader
import com.hexgram.android.ui.components.TagChip
import com.hexgram.android.ui.theme.HexgramColors
import com.hexgram.android.ui.theme.SerifFont
import com.hexgram.android.ui.theme.wuxingColor
import com.hexgram.android.viewmodels.BaziViewModel
import com.hexgram.android.models.GanZhi

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BaziScreen(
    viewModel: BaziViewModel = viewModel()
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SectionHeader(subtitle = "BAZI PAIPAN", title = "四柱八字")

        Spacer(modifier = Modifier.height(20.dp))

        // Input section
        PanelCard {
            Column {
                Text(
                    text = "出生信息",
                    fontSize = 14.sp,
                    fontFamily = SerifFont,
                    color = HexgramColors.textSecondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Name input
                OutlinedTextField(
                    value = viewModel.name,
                    onValueChange = { viewModel.name = it },
                    placeholder = {
                        Text(
                            "姓名（选填）",
                            color = HexgramColors.textTertiary,
                            fontFamily = SerifFont
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = outlinedFieldColors(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = SerifFont,
                        fontSize = 14.sp
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Date pickers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NumberPickerField(
                        label = "年",
                        value = viewModel.selectedYear,
                        onValueChange = { viewModel.selectedYear = it },
                        range = 1900..2100,
                        modifier = Modifier.weight(1f)
                    )
                    NumberPickerField(
                        label = "月",
                        value = viewModel.selectedMonth,
                        onValueChange = { viewModel.selectedMonth = it },
                        range = 1..12,
                        modifier = Modifier.weight(1f)
                    )
                    NumberPickerField(
                        label = "日",
                        value = viewModel.selectedDay,
                        onValueChange = { viewModel.selectedDay = it },
                        range = 1..31,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Hour and sex
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.weight(2f)) {
                        HourDropdown(
                            selectedIndex = viewModel.selectedHourIndex,
                            onSelect = { viewModel.selectedHourIndex = it },
                            hours = BaziViewModel.CHINESE_HOURS
                        )
                    }

                    // Sex picker
                    SexPicker(
                        selected = viewModel.sex,
                        onSelect = { viewModel.sex = it },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Buttons
        GoldButton(
            text = "排盘",
            onClick = { viewModel.calculate() }
        )

        if (viewModel.result != null) {
            Spacer(modifier = Modifier.height(8.dp))
            GhostButton(
                text = "重置",
                onClick = { viewModel.reset() }
            )
        }

        // Result display
        viewModel.result?.let { r ->
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = HexgramColors.border.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))

            // Name header
            if (r.name.isNotBlank()) {
                Text(
                    text = r.name,
                    fontSize = 20.sp,
                    fontFamily = SerifFont,
                    fontWeight = FontWeight.Bold,
                    color = HexgramColors.goldLight,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Text(
                text = "${if (r.sex == "M") "男" else "女"}命 · ${r.shengXiao}年",
                fontSize = 14.sp,
                fontFamily = SerifFont,
                color = HexgramColors.textSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Four pillars
            PanelCard {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "四柱八字",
                        fontSize = 16.sp,
                        fontFamily = SerifFont,
                        fontWeight = FontWeight.Bold,
                        color = HexgramColors.gold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Column headers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        PillarHeader("年柱")
                        PillarHeader("月柱")
                        PillarHeader("日柱")
                        PillarHeader("时柱")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Shi Shen row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (pillar in r.pillars) {
                            Text(
                                text = pillar.shiShen,
                                fontSize = 11.sp,
                                fontFamily = SerifFont,
                                color = HexgramColors.textTertiary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Tian Gan row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for ((index, pillar) in r.pillars.withIndex()) {
                            PillarGanZhiCell(
                                text = pillar.gan,
                                wuxing = pillar.wuxing,
                                isRiGan = index == 2,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Di Zhi row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (pillar in r.pillars) {
                            val zhiWx = GanZhi.wuxingDiZhi[pillar.zhi] ?: ""
                            PillarGanZhiCell(
                                text = pillar.zhi,
                                wuxing = zhiWx,
                                isRiGan = false,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Cang Gan labels
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (pillar in r.pillars) {
                            Text(
                                text = pillar.cangGan.joinToString(" ") { it.gan },
                                fontSize = 11.sp,
                                fontFamily = SerifFont,
                                color = HexgramColors.textTertiary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = HexgramColors.border.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(8.dp))

                    // Cang Gan detail
                    Text(
                        text = "藏干",
                        fontSize = 13.sp,
                        fontFamily = SerifFont,
                        color = HexgramColors.textSecondary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (pillar in r.pillars) {
                            Text(
                                text = pillar.cangGan.joinToString(" ") { "${it.gan}${it.shiShen}" },
                                fontSize = 12.sp,
                                fontFamily = SerifFont,
                                color = HexgramColors.textPrimary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Na Yin
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "纳音",
                        fontSize = 13.sp,
                        fontFamily = SerifFont,
                        color = HexgramColors.textSecondary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (ny in r.naYinPillars) {
                            Text(
                                text = ny,
                                fontSize = 12.sp,
                                fontFamily = SerifFont,
                                color = HexgramColors.textTertiary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Wuxing strength
            PanelCard {
                Column {
                    Text(
                        text = "五行力量",
                        fontSize = 14.sp,
                        fontFamily = SerifFont,
                        fontWeight = FontWeight.Bold,
                        color = HexgramColors.gold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    val wuxingNames = listOf("木", "火", "土", "金", "水")
                    val maxCount = r.wuxingCounts.values.maxOrNull()?.toFloat() ?: 1f

                    for (wx in wuxingNames) {
                        val count = r.wuxingCounts[wx] ?: 0.0
                        WuxingBar(
                            name = wx,
                            count = count,
                            fraction = if (maxCount > 0) (count / maxCount).toFloat() else 0f,
                            color = wuxingColor(wx)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = HexgramColors.border.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(8.dp))

                    InfoRow(label = "日主", value = "${r.riGan} (${r.riGanWuxing})")
                    Spacer(modifier = Modifier.height(4.dp))
                    val strengthText = if (r.isStrong) "身旺" else "身弱"
                    InfoRow(
                        label = "日主旺衰",
                        value = strengthText,
                        valueColor = if (r.isStrong)
                            HexgramColors.yiGreen else HexgramColors.jiRed
                    )
                }
            }

            // Shen Sha
            if (r.shenSha.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                PanelCard {
                    Column {
                        Text(
                            text = "神煞",
                            fontSize = 14.sp,
                            fontFamily = SerifFont,
                            fontWeight = FontWeight.Bold,
                            color = HexgramColors.gold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            for (ss in r.shenSha) {
                                TagChip(text = "${ss.name}(${ss.pillar})")
                            }
                        }
                    }
                }
            }

            // Di Zhi Relations
            if (r.diZhiRelations.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                PanelCard {
                    Column {
                        Text(
                            text = "地支关系",
                            fontSize = 14.sp,
                            fontFamily = SerifFont,
                            fontWeight = FontWeight.Bold,
                            color = HexgramColors.gold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        for (rel in r.diZhiRelations) {
                            Text(
                                text = "${rel.type}：${rel.branches}",
                                fontSize = 13.sp,
                                fontFamily = SerifFont,
                                color = HexgramColors.textPrimary,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }
                }
            }

            // Da Yun (Major cycles)
            if (r.daYun.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                PanelCard {
                    Column {
                        Text(
                            text = "大运（${if (r.isShunPai) "顺" else "逆"}排）",
                            fontSize = 14.sp,
                            fontFamily = SerifFont,
                            fontWeight = FontWeight.Bold,
                            color = HexgramColors.gold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            for (dy in r.daYun) {
                                val isCurrent = r.currentYear >= dy.year && r.currentYear < dy.year + 10
                                Column(
                                    modifier = Modifier
                                        .width(64.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isCurrent) HexgramColors.gold.copy(alpha = 0.12f)
                                            else HexgramColors.bgResult
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (isCurrent) HexgramColors.gold.copy(alpha = 0.4f)
                                            else HexgramColors.border.copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(vertical = 8.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "${dy.age}-${dy.age + 9}",
                                        fontSize = 10.sp,
                                        fontFamily = SerifFont,
                                        color = HexgramColors.textTertiary
                                    )
                                    Text(
                                        text = "${dy.gan}${dy.zhi}",
                                        fontSize = 15.sp,
                                        fontFamily = SerifFont,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isCurrent) HexgramColors.gold
                                        else HexgramColors.textPrimary
                                    )
                                    Text(
                                        text = "${dy.year}",
                                        fontSize = 10.sp,
                                        fontFamily = SerifFont,
                                        color = HexgramColors.textTertiary
                                    )
                                    if (isCurrent) {
                                        Text(
                                            text = "当前",
                                            fontSize = 9.sp,
                                            fontFamily = SerifFont,
                                            color = HexgramColors.gold,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Liu Nian (Current year)
            Spacer(modifier = Modifier.height(12.dp))
            PanelCard {
                Column {
                    Text(
                        text = "流年",
                        fontSize = 14.sp,
                        fontFamily = SerifFont,
                        fontWeight = FontWeight.Bold,
                        color = HexgramColors.gold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "${r.currentYear}年 ${r.liuNianGan}${r.liuNianZhi}（${r.liuNianShiShen}）",
                        fontSize = 14.sp,
                        fontFamily = SerifFont,
                        color = HexgramColors.textPrimary
                    )
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
                            text = "AI深度解读",
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
                    text = "AI深度解读",
                    onClick = { viewModel.requestAI() }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun PillarHeader(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontFamily = SerifFont,
        color = HexgramColors.textSecondary,
        textAlign = TextAlign.Center,
        modifier = Modifier.width(72.dp)
    )
}

@Composable
private fun PillarGanZhiCell(
    text: String,
    wuxing: String,
    isRiGan: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(horizontal = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isRiGan) HexgramColors.gold.copy(alpha = 0.15f)
                else HexgramColors.bgResult
            )
            .border(
                width = if (isRiGan) 1.dp else 0.5.dp,
                color = if (isRiGan) HexgramColors.gold.copy(alpha = 0.5f)
                else HexgramColors.border.copy(alpha = 0.4f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 22.sp,
            fontFamily = SerifFont,
            fontWeight = FontWeight.Bold,
            color = wuxingColor(wuxing)
        )
    }
}

@Composable
private fun WuxingBar(
    name: String,
    count: Double,
    fraction: Float,
    color: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            fontSize = 14.sp,
            fontFamily = SerifFont,
            color = color,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .weight(1f)
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp)),
            color = color,
            trackColor = HexgramColors.bgResult,
        )
        Spacer(modifier = Modifier.width(8.dp))
        val formatted = if (count == count.toLong().toDouble()) "${count.toInt()}" else "$count"
        Text(
            text = formatted,
            fontSize = 13.sp,
            fontFamily = SerifFont,
            color = HexgramColors.textSecondary,
            modifier = Modifier.width(28.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun SexPicker(
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf("M" to "男", "F" to "女")
    Row(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, HexgramColors.border, RoundedCornerShape(8.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for ((code, label) in options) {
            val isSelected = selected == code
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .background(
                        if (isSelected) HexgramColors.gold.copy(alpha = 0.2f)
                        else HexgramColors.bgResult
                    )
                    .clickable { onSelect(code) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = 15.sp,
                    fontFamily = SerifFont,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) HexgramColors.gold
                    else HexgramColors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun outlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = HexgramColors.textPrimary,
    unfocusedTextColor = HexgramColors.textPrimary,
    cursorColor = HexgramColors.gold,
    focusedBorderColor = HexgramColors.gold,
    unfocusedBorderColor = HexgramColors.border,
    focusedContainerColor = HexgramColors.bgResult,
    unfocusedContainerColor = HexgramColors.bgResult,
    focusedLabelColor = HexgramColors.gold,
    unfocusedLabelColor = HexgramColors.textTertiary
)

package com.hexgram.android.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.hexgram.android.ui.components.ProgressDots
import com.hexgram.android.ui.components.ResultCard
import com.hexgram.android.ui.components.SectionHeader
import com.hexgram.android.ui.components.TagChip
import com.hexgram.android.ui.components.YaoSymbol
import com.hexgram.android.ui.theme.HexgramColors
import com.hexgram.android.ui.theme.SerifFont
import com.hexgram.android.viewmodels.LiuyaoPhase
import com.hexgram.android.viewmodels.LiuyaoViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LiuyaoScreen(
    viewModel: LiuyaoViewModel = viewModel()
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SectionHeader(subtitle = "LIUYAO NAJIA", title = "六爻纳甲")

        Spacer(modifier = Modifier.height(20.dp))

        // Question input
        PanelCard {
            Column {
                Text(
                    text = "所问之事",
                    fontSize = 14.sp,
                    fontFamily = SerifFont,
                    color = HexgramColors.textSecondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = viewModel.question,
                    onValueChange = { viewModel.question = it },
                    placeholder = {
                        Text(
                            "请输入所问之事...",
                            color = HexgramColors.textTertiary,
                            fontFamily = SerifFont
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
                    maxLines = 2
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Date & Time selection
        PanelCard {
            Column {
                Text(
                    text = "起卦时间",
                    fontSize = 14.sp,
                    fontFamily = SerifFont,
                    color = HexgramColors.textSecondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Year
                    NumberPickerField(
                        label = "年",
                        value = viewModel.selectedYear,
                        onValueChange = { viewModel.selectedYear = it },
                        range = 1900..2100,
                        modifier = Modifier.weight(1f)
                    )
                    // Month
                    NumberPickerField(
                        label = "月",
                        value = viewModel.selectedMonth,
                        onValueChange = { viewModel.selectedMonth = it },
                        range = 1..12,
                        modifier = Modifier.weight(1f)
                    )
                    // Day
                    NumberPickerField(
                        label = "日",
                        value = viewModel.selectedDay,
                        onValueChange = { viewModel.selectedDay = it },
                        range = 1..31,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Hour picker
                HourDropdown(
                    selectedIndex = viewModel.selectedHourIndex,
                    onSelect = { viewModel.selectedHourIndex = it },
                    hours = LiuyaoViewModel.CHINESE_HOURS
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Progress dots
        ProgressDots(
            total = 6,
            filled = viewModel.lines.size,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Text(
            text = if (viewModel.lines.size < 6)
                "第 ${viewModel.lines.size + 1} 爻 / 共 6 爻"
            else "六爻已成",
            fontSize = 13.sp,
            fontFamily = SerifFont,
            color = HexgramColors.textSecondary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Yao display (building up from bottom)
        if (viewModel.lines.isNotEmpty()) {
            PanelCard {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val yaoNames = listOf("初爻", "二爻", "三爻", "四爻", "五爻", "上爻")
                    // Display from top (上爻) to bottom (初爻) visually
                    for (i in (viewModel.lines.size - 1) downTo 0) {
                        val value = viewModel.lines[i]
                        val isYang = value == 7 || value == 9
                        val isChanging = value == 6 || value == 9
                        val label = yaoNames[i]
                        val desc = when (value) {
                            6 -> "老阴 ⚬"
                            7 -> "少阳"
                            8 -> "少阴"
                            9 -> "老阳 ⚬"
                            else -> ""
                        }

                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + slideInVertically(
                                initialOffsetY = { it / 2 },
                                animationSpec = spring(stiffness = Spring.StiffnessMedium)
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontFamily = SerifFont,
                                    color = HexgramColors.textTertiary,
                                    modifier = Modifier.width(40.dp),
                                    textAlign = TextAlign.End
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                YaoSymbol(
                                    isYang = isYang,
                                    isChanging = isChanging,
                                    width = 140.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = desc,
                                    fontSize = 12.sp,
                                    fontFamily = SerifFont,
                                    color = if (isChanging) HexgramColors.dongYao
                                    else HexgramColors.textTertiary,
                                    modifier = Modifier.width(60.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (viewModel.lines.isNotEmpty()) {
                GhostButton(
                    text = "撤回",
                    onClick = { viewModel.undo() },
                    modifier = Modifier.weight(1f)
                )
            }

            if (viewModel.lines.size < 6) {
                GoldButton(
                    text = "☰ 摇卦",
                    onClick = { viewModel.toss() },
                    modifier = Modifier.weight(if (viewModel.lines.isNotEmpty()) 1f else 1f)
                )
            }
        }

        if (viewModel.lines.size >= 6 && viewModel.guaResult == null) {
            Spacer(modifier = Modifier.height(12.dp))
            GoldButton(
                text = "排盘解卦",
                onClick = { viewModel.doReading() }
            )
        }

        if (viewModel.guaResult != null || viewModel.lines.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            GhostButton(
                text = "重新起卦",
                onClick = { viewModel.reset() }
            )
        }

        // Result display
        if (viewModel.guaResult != null) {
            Spacer(modifier = Modifier.height(24.dp))

            HorizontalDivider(color = HexgramColors.border.copy(alpha = 0.5f))

            Spacer(modifier = Modifier.height(16.dp))

            GuaResultDisplay(viewModel)
        }

        // Classics display
        if (viewModel.classicsText.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            ResultCard {
                Column {
                    Text(
                        text = "经典文献参照",
                        fontSize = 16.sp,
                        fontFamily = SerifFont,
                        fontWeight = FontWeight.Bold,
                        color = HexgramColors.gold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    MarkdownText(text = viewModel.classicsText)
                }
            }
        }

        // AI section
        if (viewModel.guaResult != null) {
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GuaResultDisplay(viewModel: LiuyaoViewModel) {
    val result = viewModel.guaResult ?: return

    // Gua name header
    PanelCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = result.benGuaName,
                fontSize = 24.sp,
                fontFamily = SerifFont,
                fontWeight = FontWeight.Bold,
                color = HexgramColors.goldLight
            )

            if (result.bianGuaName.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "之",
                        fontSize = 14.sp,
                        fontFamily = SerifFont,
                        color = HexgramColors.textSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Text(
                        text = result.bianGuaName,
                        fontSize = 20.sp,
                        fontFamily = SerifFont,
                        fontWeight = FontWeight.Bold,
                        color = HexgramColors.accent
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Gua palace & attribute info
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TagChip(text = "${result.gong}宫")
                if (result.shiYao >= 0) {
                    TagChip(text = "世在${yaoPositionName(result.shiYao)}")
                }
            }

            // Special markers
            if (result.specialMarkers.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (marker in result.specialMarkers) {
                        val color = when {
                            marker.contains("冲") || marker.contains("破") || marker.contains("克") ->
                                HexgramColors.jiRed
                            marker.contains("合") || marker.contains("生") ->
                                HexgramColors.yiGreen
                            else -> HexgramColors.accent
                        }
                        TagChip(text = marker, color = color)
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Gua table: 六神 | 六亲 | 本卦 | 变卦
    ResultCard {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "卦象详表",
                fontSize = 16.sp,
                fontFamily = SerifFont,
                fontWeight = FontWeight.Bold,
                color = HexgramColors.gold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Table header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HexgramColors.bgPanel)
                    .padding(vertical = 6.dp, horizontal = 4.dp)
            ) {
                TableCell("六神", Modifier.weight(1f), HexgramColors.textSecondary)
                TableCell("六亲", Modifier.weight(1f), HexgramColors.textSecondary)
                TableCell("本卦", Modifier.weight(1.5f), HexgramColors.textSecondary)
                TableCell("变卦", Modifier.weight(1.5f), HexgramColors.textSecondary)
            }

            HorizontalDivider(color = HexgramColors.border.copy(alpha = 0.5f))

            // Table rows (top to bottom = 上爻 to 初爻)
            for (i in 5 downTo 0) {
                val yao = result.yaos[i]
                val isShi = i == result.shiYao
                val isYing = i == result.yingYao

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .let {
                            if (isShi || isYing) it.background(
                                HexgramColors.gold.copy(alpha = 0.06f)
                            ) else it
                        }
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 六神
                    TableCell(
                        text = yao.liuShen,
                        modifier = Modifier.weight(1f),
                        color = liuShenColor(yao.liuShen)
                    )

                    // 六亲 + 世/应 marker
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = yao.liuQin,
                            fontSize = 13.sp,
                            fontFamily = SerifFont,
                            color = HexgramColors.textPrimary,
                            textAlign = TextAlign.Center
                        )
                        if (isShi) {
                            Text(
                                text = "世",
                                fontSize = 10.sp,
                                fontFamily = SerifFont,
                                color = HexgramColors.gold,
                                fontWeight = FontWeight.Bold
                            )
                        } else if (isYing) {
                            Text(
                                text = "应",
                                fontSize = 10.sp,
                                fontFamily = SerifFont,
                                color = HexgramColors.goldDark,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // 本卦爻
                    Column(
                        modifier = Modifier.weight(1.5f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            YaoSymbol(
                                isYang = yao.isYang,
                                isChanging = yao.isChanging,
                                width = 48.dp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${yao.naJiaGan}${yao.naJiaZhi}",
                                fontSize = 12.sp,
                                fontFamily = SerifFont,
                                color = if (yao.isChanging) HexgramColors.dongYao
                                else HexgramColors.textPrimary
                            )
                        }
                        if (yao.isKongWang) {
                            Text(
                                text = "空",
                                fontSize = 10.sp,
                                fontFamily = SerifFont,
                                color = HexgramColors.jiRed
                            )
                        }
                    }

                    // 变卦爻
                    Column(
                        modifier = Modifier.weight(1.5f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (yao.isChanging && yao.bianYao != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                YaoSymbol(
                                    isYang = !yao.isYang,
                                    isChanging = false,
                                    width = 48.dp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = yao.bianYao?.let {
                                        "${it.naJiaGan}${it.naJiaZhi}"
                                    } ?: "",
                                    fontSize = 12.sp,
                                    fontFamily = SerifFont,
                                    color = HexgramColors.textSecondary
                                )
                            }
                            if (yao.dongBianDesc.isNotBlank()) {
                                Text(
                                    text = yao.dongBianDesc,
                                    fontSize = 10.sp,
                                    fontFamily = SerifFont,
                                    color = when {
                                        yao.dongBianDesc.contains("回头生") -> HexgramColors.yiGreen
                                        yao.dongBianDesc.contains("回头克") || yao.dongBianDesc.contains("化墓") || yao.dongBianDesc.contains("化绝") -> HexgramColors.jiRed
                                        else -> HexgramColors.textTertiary
                                    }
                                )
                            }
                        }
                    }
                }

                if (i > 0) {
                    HorizontalDivider(
                        color = HexgramColors.border.copy(alpha = 0.3f),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        }
    }

    // 伏神 section
    if (result.fuShen.isNotEmpty()) {
        Spacer(modifier = Modifier.height(12.dp))
        PanelCard {
            Column {
                Text(
                    text = "伏神",
                    fontSize = 14.sp,
                    fontFamily = SerifFont,
                    fontWeight = FontWeight.Bold,
                    color = HexgramColors.gold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                for (fu in result.fuShen) {
                    InfoRow(
                        label = "${fu.position} 伏 ${fu.liuQin}",
                        value = "${fu.ganZhi}（${fu.wuxing}）"
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }

    // 空亡 & 月建 info
    Spacer(modifier = Modifier.height(12.dp))
    PanelCard {
        Column {
            InfoRow(label = "日辰", value = result.riGanZhi)
            Spacer(modifier = Modifier.height(4.dp))
            InfoRow(label = "月建", value = result.yueJian)
            Spacer(modifier = Modifier.height(4.dp))
            InfoRow(
                label = "空亡",
                value = result.kongWang.joinToString("、"),
                valueColor = HexgramColors.jiRed
            )
        }
    }
}

@Composable
private fun TableCell(
    text: String,
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color = HexgramColors.textPrimary
) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontFamily = SerifFont,
        color = color,
        textAlign = TextAlign.Center,
        modifier = modifier
    )
}

private fun liuShenColor(name: String): androidx.compose.ui.graphics.Color = when (name) {
    "青龙" -> HexgramColors.wood
    "朱雀" -> HexgramColors.fire
    "勾陈" -> HexgramColors.earth
    "螣蛇" -> HexgramColors.earth
    "白虎" -> HexgramColors.metal
    "玄武" -> HexgramColors.water
    else -> HexgramColors.textSecondary
}

private fun yaoPositionName(index: Int): String = when (index) {
    0 -> "初爻"
    1 -> "二爻"
    2 -> "三爻"
    3 -> "四爻"
    4 -> "五爻"
    5 -> "上爻"
    else -> ""
}

@Composable
fun NumberPickerField(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value.toString(),
        onValueChange = { text ->
            val num = text.filter { it.isDigit() }.toIntOrNull()
            if (num != null && num in range) {
                onValueChange(num)
            } else if (text.isEmpty()) {
                onValueChange(range.first)
            }
        },
        label = {
            Text(
                label,
                fontSize = 12.sp,
                fontFamily = SerifFont,
                color = HexgramColors.textTertiary
            )
        },
        modifier = modifier,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = HexgramColors.textPrimary,
            unfocusedTextColor = HexgramColors.textPrimary,
            cursorColor = HexgramColors.gold,
            focusedBorderColor = HexgramColors.gold,
            unfocusedBorderColor = HexgramColors.border,
            focusedContainerColor = HexgramColors.bgResult,
            unfocusedContainerColor = HexgramColors.bgResult,
            focusedLabelColor = HexgramColors.gold,
            unfocusedLabelColor = HexgramColors.textTertiary
        ),
        shape = RoundedCornerShape(8.dp),
        singleLine = true,
        textStyle = androidx.compose.ui.text.TextStyle(
            fontFamily = SerifFont,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    )
}

@Composable
fun HourDropdown(
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    hours: List<String>,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = hours[selectedIndex],
            onValueChange = {},
            readOnly = true,
            label = {
                Text(
                    "时辰",
                    fontSize = 12.sp,
                    fontFamily = SerifFont,
                    color = HexgramColors.textTertiary
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = HexgramColors.textPrimary,
                unfocusedTextColor = HexgramColors.textPrimary,
                focusedBorderColor = HexgramColors.gold,
                unfocusedBorderColor = HexgramColors.border,
                focusedContainerColor = HexgramColors.bgResult,
                unfocusedContainerColor = HexgramColors.bgResult,
                focusedLabelColor = HexgramColors.gold,
                unfocusedLabelColor = HexgramColors.textTertiary
            ),
            shape = RoundedCornerShape(8.dp),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontFamily = SerifFont,
                fontSize = 14.sp
            ),
            enabled = false // Makes it clickable without keyboard
        )

        // Invisible overlay for click
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { expanded = true }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(HexgramColors.bgPanel)
        ) {
            hours.forEachIndexed { index, hour ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = hour,
                            fontFamily = SerifFont,
                            fontSize = 14.sp,
                            color = if (index == selectedIndex) HexgramColors.gold
                            else HexgramColors.textPrimary
                        )
                    },
                    onClick = {
                        onSelect(index)
                        expanded = false
                    }
                )
            }
        }
    }
}

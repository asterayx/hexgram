package com.hexgram.android.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hexgram.android.ui.components.GhostButton
import com.hexgram.android.ui.components.GoldButton
import com.hexgram.android.ui.components.LoadingSpinner
import com.hexgram.android.ui.components.MarkdownText
import com.hexgram.android.ui.components.PanelCard
import com.hexgram.android.ui.components.ResultCard
import com.hexgram.android.ui.components.SectionHeader
import com.hexgram.android.ui.components.YaoSymbol
import com.hexgram.android.ui.theme.HexgramColors
import com.hexgram.android.ui.theme.SerifFont
import com.hexgram.android.ui.theme.wuxingColor
import com.hexgram.android.viewmodels.LiuyaoViewModel
import com.hexgram.android.models.GuaResult
import com.hexgram.android.models.YAO_NAMES
import com.hexgram.android.models.YAO_LABELS

@Composable
fun LiuyaoScreen(viewModel: LiuyaoViewModel = viewModel()) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SectionHeader(subtitle = "纳 甲 六 爻", title = "易经排盘")
        Spacer(modifier = Modifier.height(20.dp))

        // Question input
        PanelCard {
            Column {
                Text("所问之事", fontSize = 14.sp, fontFamily = SerifFont, color = HexgramColors.textSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = viewModel.question,
                    onValueChange = { viewModel.question = it },
                    placeholder = { Text("心中所问之事（可留空）", color = HexgramColors.textTertiary, fontFamily = SerifFont) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = HexgramColors.textPrimary, unfocusedTextColor = HexgramColors.textPrimary,
                        cursorColor = HexgramColors.gold, focusedBorderColor = HexgramColors.gold, unfocusedBorderColor = HexgramColors.border,
                        focusedContainerColor = HexgramColors.bgResult, unfocusedContainerColor = HexgramColors.bgResult
                    ),
                    shape = RoundedCornerShape(8.dp), maxLines = 2
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Date & Time
        PanelCard {
            Column {
                Text("起卦时间", fontSize = 14.sp, fontFamily = SerifFont, color = HexgramColors.textSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberPickerField("年", viewModel.selectedYear, { viewModel.selectedYear = it }, 1900..2100, Modifier.weight(1f))
                    NumberPickerField("月", viewModel.selectedMonth, { viewModel.selectedMonth = it }, 1..12, Modifier.weight(1f))
                    NumberPickerField("日", viewModel.selectedDay, { viewModel.selectedDay = it }, 1..31, Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                HourDropdown(viewModel.selectedHourIndex, { viewModel.selectedHourIndex = it }, LiuyaoViewModel.CHINESE_HOURS)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Progress
        Text(
            text = if (viewModel.lines.size < 6) "第${YAO_NAMES.getOrElse(viewModel.lines.size) { "" }}爻（${viewModel.lines.size}/6）" else "卦象已成",
            fontSize = 13.sp, fontFamily = SerifFont, color = HexgramColors.textSecondary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Yao display
        if (viewModel.lines.isNotEmpty()) {
            PanelCard {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (i in (viewModel.lines.size - 1) downTo 0) {
                        val value = viewModel.lines[i]
                        val isYang = value == 7 || value == 9
                        val isChanging = value == 6 || value == 9
                        AnimatedVisibility(visible = true, enter = fadeIn() + slideInVertically { it / 2 }) {
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                Text(YAO_NAMES[i], fontSize = 12.sp, fontFamily = SerifFont, color = HexgramColors.textTertiary, modifier = Modifier.width(30.dp), textAlign = TextAlign.End)
                                Spacer(modifier = Modifier.width(8.dp))
                                YaoSymbol(isYang = isYang, isChanging = isChanging, width = 140.dp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(YAO_LABELS[value] ?: "", fontSize = 12.sp, fontFamily = SerifFont, color = if (isChanging) HexgramColors.accent else HexgramColors.textTertiary, modifier = Modifier.width(50.dp))
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Buttons
        if (viewModel.lines.size < 6) {
            GoldButton(if (viewModel.isTossing) "摇卦中…" else "🪙 摇卦", { viewModel.toss() })
        }

        if (viewModel.lines.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            GhostButton("撤回", { viewModel.undo() })
        }

        if (viewModel.lines.size >= 6 && viewModel.guaResult == null) {
            Spacer(modifier = Modifier.height(12.dp))
            GoldButton("排盘解卦", { viewModel.doReading() })
        }

        if (viewModel.lines.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            GhostButton("重来", { viewModel.reset() })
        }

        // Result
        viewModel.guaResult?.let { gua ->
            Spacer(modifier = Modifier.height(24.dp))
            GuaResultView(gua)
        }

        // Full text result
        if (viewModel.resultText.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            ResultCard {
                MarkdownText(viewModel.resultText)
            }

            Spacer(modifier = Modifier.height(12.dp))
            GoldButton(
                text = if (viewModel.aiLoading) "解读中，请稍候…" else "🤖 AI深度解读",
                onClick = { viewModel.requestAI() },
                enabled = !viewModel.aiLoading
            )
            Spacer(modifier = Modifier.height(8.dp))
            GhostButton("再占一卦", { viewModel.reset() })
        }

        // AI loading
        if (viewModel.aiLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            LoadingSpinner("卦师正在参详卦象…")
        }

        // AI result
        if (viewModel.aiText.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            ResultCard {
                Column {
                    Text("AI深度解读", fontSize = 15.sp, fontFamily = SerifFont, fontWeight = FontWeight.Bold, color = HexgramColors.goldLight)
                    Spacer(modifier = Modifier.height(8.dp))
                    MarkdownText(viewModel.aiText)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun GuaResultView(gua: GuaResult) {
    PanelCard {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("${gua.gong}宫 · ${gua.guaName}卦${if (gua.youHun) "（游魂）" else ""}${if (gua.guiHun) "（归魂）" else ""}",
                fontSize = 16.sp, fontFamily = SerifFont, fontWeight = FontWeight.Medium, color = HexgramColors.goldLight)
            Spacer(modifier = Modifier.height(4.dp))
            if (gua.isLiuChongGua) Text("⚡ 六冲卦", fontSize = 11.sp, fontFamily = SerifFont, color = HexgramColors.accent)
            if (gua.isLiuHeGua) Text("🤝 六合卦", fontSize = 11.sp, fontFamily = SerifFont, color = HexgramColors.yiGreen)
            if (gua.isFanYin) Text("⚠ 反吟", fontSize = 11.sp, fontFamily = SerifFont, color = HexgramColors.jiRed)
            if (gua.isFuYin) Text("😩 伏吟", fontSize = 11.sp, fontFamily = SerifFont, color = HexgramColors.jiRed)
            Spacer(modifier = Modifier.height(8.dp))
            Text("空亡：${gua.kongWang.joinToString("·")}", fontSize = 11.sp, fontFamily = SerifFont, color = HexgramColors.jiRed)

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = HexgramColors.border.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            // Header
            Row(modifier = Modifier.fillMaxWidth().background(HexgramColors.bgPanel).padding(vertical = 6.dp)) {
                Text("六神", fontSize = 10.sp, fontFamily = SerifFont, color = HexgramColors.gold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Text("六亲", fontSize = 10.sp, fontFamily = SerifFont, color = HexgramColors.gold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Text("本卦 ${gua.guaName}", fontSize = 10.sp, fontFamily = SerifFont, color = HexgramColors.gold, modifier = Modifier.weight(2f), textAlign = TextAlign.Center)
                if (gua.hasChanging) Text("变卦 ${gua.changedGuaName ?: ""}", fontSize = 10.sp, fontFamily = SerifFont, color = HexgramColors.gold, modifier = Modifier.weight(2f), textAlign = TextAlign.Center)
            }
            HorizontalDivider(color = HexgramColors.border)

            // Rows
            for (i in 5 downTo 0) {
                val y = gua.yaos[i]
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(y.liuShen, fontSize = 10.sp, fontFamily = SerifFont, color = HexgramColors.textSecondary, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(y.liuqin, fontSize = 10.sp, fontFamily = SerifFont, color = HexgramColors.goldLight, textAlign = TextAlign.Center)
                        if (y.isShi) Text("世", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = HexgramColors.accent)
                        if (y.isYing) Text("应", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = HexgramColors.gold)
                    }
                    Row(modifier = Modifier.weight(2f), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Text("${y.tianGan}${y.diZhi}", fontSize = 11.sp, fontFamily = SerifFont, color = HexgramColors.textPrimary)
                        Text(y.wuxing, fontSize = 9.sp, color = wuxingColor(y.wuxing), modifier = Modifier.padding(start = 4.dp))
                        YaoSymbol(isYang = y.yinYang == "阳", isChanging = y.isDong, width = 36.dp)
                        if (y.isKong) Text("空", fontSize = 8.sp, color = HexgramColors.jiRed, modifier = Modifier.padding(start = 2.dp))
                        if (y.isDong) Text("○", fontSize = 10.sp, color = HexgramColors.accent, modifier = Modifier.padding(start = 2.dp))
                    }
                    if (gua.hasChanging) {
                        val cYaos = gua.changedYaos
                        if (y.isDong && cYaos != null) {
                            val c = cYaos[i]
                            Row(modifier = Modifier.weight(2f), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                                Text(c.liuqin, fontSize = 10.sp, fontFamily = SerifFont, color = HexgramColors.goldLight)
                                Text("${c.tianGan}${c.diZhi}", fontSize = 11.sp, fontFamily = SerifFont, color = HexgramColors.textPrimary, modifier = Modifier.padding(start = 4.dp))
                                Text(c.wuxing, fontSize = 9.sp, color = wuxingColor(c.wuxing), modifier = Modifier.padding(start = 4.dp))
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(2f))
                        }
                    }
                }
                if (i > 0) HorizontalDivider(color = HexgramColors.border.copy(alpha = 0.3f))
            }
        }
    }
}

@Composable
fun NumberPickerField(label: String, value: Int, onValueChange: (Int) -> Unit, range: IntRange, modifier: Modifier = Modifier) {
    var textState by remember(value) { mutableStateOf(value.toString()) }
    OutlinedTextField(
        value = textState,
        onValueChange = { text ->
            val digits = text.filter { it.isDigit() }
            if (digits.length <= range.last.toString().length) {
                textState = digits
                digits.toIntOrNull()?.let { if (it in range) onValueChange(it) }
            }
        },
        label = { Text(label, fontSize = 12.sp, fontFamily = SerifFont, color = HexgramColors.textTertiary) },
        modifier = modifier,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = HexgramColors.textPrimary, unfocusedTextColor = HexgramColors.textPrimary,
            cursorColor = HexgramColors.gold, focusedBorderColor = HexgramColors.gold, unfocusedBorderColor = HexgramColors.border,
            focusedContainerColor = HexgramColors.bgResult, unfocusedContainerColor = HexgramColors.bgResult
        ),
        shape = RoundedCornerShape(8.dp), singleLine = true,
        textStyle = androidx.compose.ui.text.TextStyle(fontFamily = SerifFont, fontSize = 14.sp, textAlign = TextAlign.Center)
    )
}

@Composable
fun HourDropdown(selectedIndex: Int, onSelect: (Int) -> Unit, hours: List<String>, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        OutlinedTextField(
            value = hours[selectedIndex], onValueChange = {}, readOnly = true,
            label = { Text("时辰", fontSize = 12.sp, fontFamily = SerifFont, color = HexgramColors.textTertiary) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = HexgramColors.textPrimary, unfocusedTextColor = HexgramColors.textPrimary,
                focusedBorderColor = HexgramColors.gold, unfocusedBorderColor = HexgramColors.border,
                focusedContainerColor = HexgramColors.bgResult, unfocusedContainerColor = HexgramColors.bgResult
            ),
            shape = RoundedCornerShape(8.dp),
            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = SerifFont, fontSize = 14.sp),
            enabled = false
        )
        Box(modifier = Modifier.matchParentSize().clickable { expanded = true })
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(HexgramColors.bgPanel)) {
            hours.forEachIndexed { index, hour ->
                DropdownMenuItem(
                    text = { Text(hour, fontFamily = SerifFont, fontSize = 14.sp, color = if (index == selectedIndex) HexgramColors.gold else HexgramColors.textPrimary) },
                    onClick = { onSelect(index); expanded = false }
                )
            }
        }
    }
}

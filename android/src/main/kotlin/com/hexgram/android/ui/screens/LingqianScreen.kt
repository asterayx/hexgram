package com.hexgram.android.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.KeyboardType
import com.hexgram.android.ui.components.GhostButton
import com.hexgram.android.ui.components.GoldButton
import com.hexgram.android.ui.components.MarkdownText
import com.hexgram.android.ui.components.PanelCard
import com.hexgram.android.ui.components.ResultCard
import com.hexgram.android.ui.components.SectionHeader
import com.hexgram.android.ui.components.ThinkingButton
import com.hexgram.android.ui.share.ShareService
import com.hexgram.android.ui.theme.HexgramColors
import com.hexgram.android.viewmodels.LINGQIAN_CATEGORIES
import com.hexgram.android.viewmodels.LingqianViewModel

@Composable
fun LingqianScreen(viewModel: LingqianViewModel = viewModel()) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SectionHeader(subtitle = "北 帝 灵 签", title = "玄天大帝")

        // 输入区
        if (viewModel.phase == LingqianViewModel.Phase.INPUT) {
            InputSection(viewModel)
        }

        // 摇签区
        ShakeSection(viewModel)

        // 签文结果
        if (viewModel.phase == LingqianViewModel.Phase.RESULT && viewModel.resultText.isNotEmpty()) {
            ResultSection(viewModel)
        }

        // AI解读
        AISection(viewModel)

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun InputSection(vm: LingqianViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = vm.question,
            onValueChange = { vm.question = it },
            placeholder = { Text("心中所问之事（可留空）", color = HexgramColors.textTertiary) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = HexgramColors.textPrimary,
                unfocusedTextColor = HexgramColors.textPrimary,
                focusedBorderColor = HexgramColors.gold,
                unfocusedBorderColor = HexgramColors.border,
                cursorColor = HexgramColors.gold,
            ),
            singleLine = true
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // 事类下拉
            LingqianCategoryDropdown(
                selectedIndex = vm.selectedCategoryIndex,
                onSelect = { vm.selectedCategoryIndex = it }
            )

            // 性别选择
            Column {
                Text("性别", fontSize = 11.sp, color = HexgramColors.textSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                    listOf("男" to 0, "女" to 1).forEach { (label, idx) ->
                        val selected = vm.selectedGender == idx
                        Text(
                            text = label,
                            color = if (selected) HexgramColors.bgPrimary else HexgramColors.gold,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(if (idx == 0) 6.dp else 0.dp, if (idx == 1) 6.dp else 0.dp, if (idx == 1) 6.dp else 0.dp, if (idx == 0) 6.dp else 0.dp))
                                .background(if (selected) HexgramColors.gold else HexgramColors.bgPrimaryPanel)
                                .border(1.dp, HexgramColors.gold, RoundedCornerShape(if (idx == 0) 6.dp else 0.dp, if (idx == 1) 6.dp else 0.dp, if (idx == 1) 6.dp else 0.dp, if (idx == 0) 6.dp else 0.dp))
                                .clickable { vm.selectedGender = idx }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            // 年龄输入
            Column {
                Text("年龄", fontSize = 11.sp, color = HexgramColors.textSecondary)
                OutlinedTextField(
                    value = vm.ageText,
                    onValueChange = { vm.ageText = it.filter { c -> c.isDigit() } },
                    placeholder = { Text("岁", color = HexgramColors.textTertiary, fontSize = 14.sp) },
                    modifier = Modifier.widthIn(max = 70.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = HexgramColors.textPrimary,
                        unfocusedTextColor = HexgramColors.textPrimary,
                        focusedBorderColor = HexgramColors.gold,
                        unfocusedBorderColor = HexgramColors.border,
                        cursorColor = HexgramColors.gold,
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }
        }
    }
}

@Composable
private fun LingqianCategoryDropdown(selectedIndex: Int, onSelect: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text("事类", fontSize = 11.sp, color = HexgramColors.textSecondary)
        Box {
            Text(
                text = LINGQIAN_CATEGORIES[selectedIndex].label,
                color = HexgramColors.gold,
                fontSize = 14.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(HexgramColors.bgPrimaryPanel)
                    .clickable { expanded = true }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(HexgramColors.bgPrimaryPanel)
            ) {
                LINGQIAN_CATEGORIES.forEachIndexed { index, cat ->
                    DropdownMenuItem(
                        text = { Text(cat.label, color = HexgramColors.textPrimary) },
                        onClick = {
                            onSelect(index)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ShakeSection(vm: LingqianViewModel) {
    PanelCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (vm.phase) {
                LingqianViewModel.Phase.SHAKING -> {
                    // 摇签动画
                    val infiniteTransition = rememberInfiniteTransition(label = "shake")
                    val rotation by infiniteTransition.animateFloat(
                        initialValue = -15f,
                        targetValue = 15f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(150),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "rotation"
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    Text("🏮", fontSize = 60.sp, modifier = Modifier.rotate(rotation))
                    LinearProgressIndicator(
                        progress = { vm.shakeProgress },
                        modifier = Modifier.width(120.dp),
                        color = HexgramColors.gold,
                        trackColor = HexgramColors.border,
                    )
                    Text("虔心摇签中…", fontSize = 13.sp, color = HexgramColors.textSecondary)
                    Spacer(modifier = Modifier.height(20.dp))
                }

                LingqianViewModel.Phase.INPUT -> {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("🏮", fontSize = 50.sp, modifier = Modifier.padding(8.dp))
                    Text(
                        "诚心默念所求之事\n点击下方按钮摇签",
                        fontSize = 12.sp,
                        color = HexgramColors.textTertiary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                LingqianViewModel.Phase.RESULT -> {
                    val q = vm.qianResult
                    if (q != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "第${q.qianNum}签",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Medium,
                            color = HexgramColors.goldLight
                        )
                        Text(q.qianName, fontSize = 16.sp, color = HexgramColors.gold)
                        if (q.guaXiang.isNotEmpty()) {
                            Text(
                                q.guaXiang,
                                fontSize = 12.sp,
                                color = guaXiangColor(q.guaXiang),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(guaXiangColor(q.guaXiang).copy(alpha = 0.12f))
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            // 操作按钮
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (vm.phase == LingqianViewModel.Phase.INPUT) {
                    GoldButton(text = "🏮 摇签", onClick = { vm.shake() })
                } else if (vm.phase == LingqianViewModel.Phase.RESULT) {
                    GhostButton(text = "再求一签", onClick = { vm.reset() })
                }
            }
        }
    }
}

@Composable
private fun ResultSection(vm: LingqianViewModel) {
    val context = LocalContext.current
    val density = LocalDensity.current

    ResultCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            MarkdownText(vm.resultText)

            if (vm.detailText.isNotEmpty()) {
                MarkdownText(vm.detailText)
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (vm.aiLoading) {
                    ThinkingButton(text = "道长正在解签…")
                } else {
                    GoldButton(text = "🤖 AI详细解签", onClick = { vm.requestAI() })
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GhostButton(
                    text = "分享灵签",
                    onClick = {
                        val widthPx = with(density) { 360.dp.roundToPx() }
                        ShareService.shareComposable(context, widthPx, "北帝灵签") {
                            LingqianShareCard(vm)
                        }
                    }
                )
                GhostButton(text = "再求一签", onClick = { vm.reset() })
            }
        }
    }
}

@Composable
private fun LingqianShareCard(vm: LingqianViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(HexgramColors.bgPrimary)
            .padding(16.dp)
    ) {
        Text(
            text = vm.resultText + vm.detailText,
            color = HexgramColors.textPrimary,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun AISection(vm: LingqianViewModel) {
    if (vm.aiError.isNotEmpty()) {
        ResultCard {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("解签失败", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = HexgramColors.jiRed)
                Text(vm.aiError, fontSize = 12.sp, color = HexgramColors.jiRed.copy(alpha = 0.7f))
            }
        }
    } else if (vm.aiText.isNotEmpty()) {
        ResultCard {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("AI详细解签", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = HexgramColors.goldLight)
                MarkdownText(vm.aiText)
            }
        }
    }
}

private fun guaXiangColor(gx: String): androidx.compose.ui.graphics.Color {
    return when {
        gx.contains("上上") -> HexgramColors.yiGreen
        gx.contains("上") -> HexgramColors.gold
        gx.contains("中平") -> HexgramColors.textSecondary
        gx.contains("下下") -> HexgramColors.jiRed
        gx.contains("下") -> HexgramColors.accent
        else -> HexgramColors.textSecondary
    }
}

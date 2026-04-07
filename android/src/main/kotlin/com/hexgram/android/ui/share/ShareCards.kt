package com.hexgram.android.ui.share

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hexgram.android.models.BaziResult
import com.hexgram.android.models.GuaResult
import com.hexgram.android.models.HuangliResult
import com.hexgram.android.ui.theme.HexgramColors
import com.hexgram.android.ui.theme.SerifFont
import com.hexgram.android.ui.theme.wuxingColor

@Composable
fun LiuyaoShareCard(gua: GuaResult) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(HexgramColors.bgPrimary)
            .padding(16.dp)
    ) {
        // Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = "${gua.gong}宫 · ${gua.guaName}卦",
                    fontSize = 20.sp,
                    fontFamily = SerifFont,
                    fontWeight = FontWeight.Medium,
                    color = HexgramColors.goldLight
                )
                if (gua.hasChanging) {
                    Text(
                        text = "→ 变卦 ${gua.changedGuaName ?: ""}",
                        fontSize = 13.sp,
                        fontFamily = SerifFont,
                        color = HexgramColors.accent
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${gua.outerGua}上·${gua.innerGua}下",
                    fontSize = 11.sp,
                    fontFamily = SerifFont,
                    color = HexgramColors.textSecondary
                )
                Text(
                    text = "日建${gua.riGan}${gua.riZhi}　月建${gua.yueZhi}月",
                    fontSize = 11.sp,
                    fontFamily = SerifFont,
                    color = HexgramColors.textSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = HexgramColors.border)
        Spacer(modifier = Modifier.height(8.dp))

        // Yao table
        for (i in 5 downTo 0) {
            val y = gua.yaos[i]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = y.liuShen,
                    fontSize = 10.sp,
                    fontFamily = SerifFont,
                    color = HexgramColors.textSecondary,
                    modifier = Modifier.width(30.dp),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = y.liuqin,
                    fontSize = 10.sp,
                    fontFamily = SerifFont,
                    color = HexgramColors.goldLight,
                    modifier = Modifier.width(30.dp),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "${y.tianGan}${y.diZhi}",
                    fontSize = 12.sp,
                    fontFamily = SerifFont,
                    color = HexgramColors.textPrimary,
                    modifier = Modifier.width(36.dp)
                )
                Text(
                    text = y.wuxing,
                    fontSize = 9.sp,
                    color = HexgramColors.textSecondary,
                    modifier = Modifier.width(16.dp)
                )

                // Yao line
                ShareYaoSymbol(
                    isYang = y.yinYang == "阳",
                    isChanging = y.isDong,
                    modifier = Modifier.width(30.dp)
                )

                // Shi/Ying
                Text(
                    text = when {
                        y.isShi -> "世"
                        y.isYing -> "应"
                        else -> "　"
                    },
                    fontSize = 9.sp,
                    fontFamily = SerifFont,
                    fontWeight = FontWeight.Bold,
                    color = if (y.isShi) HexgramColors.accent else HexgramColors.gold,
                    modifier = Modifier.width(16.dp)
                )

                // Changed yao
                if (y.isDong && gua.changedYaos != null) {
                    val c = gua.changedYaos[i]
                    Text(
                        text = "→${c.liuqin}${c.tianGan}${c.diZhi}",
                        fontSize = 10.sp,
                        fontFamily = SerifFont,
                        color = HexgramColors.accent
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = HexgramColors.border)
        Spacer(modifier = Modifier.height(8.dp))

        // Footer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "易学三合 · 六爻纳甲排盘",
                fontSize = 9.sp,
                fontFamily = SerifFont,
                color = HexgramColors.textTertiary
            )
            Text(
                text = "空亡：${gua.kongWang.joinToString("·")}",
                fontSize = 9.sp,
                fontFamily = SerifFont,
                color = HexgramColors.textTertiary
            )
        }
    }
}

@Composable
fun BaziShareCard(result: BaziResult) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(HexgramColors.bgPrimary)
            .padding(16.dp)
    ) {
        // Title
        Text(
            text = "${if (result.name.isNotBlank()) "${result.name}的" else ""}八字命盘",
            fontSize = 18.sp,
            fontFamily = SerifFont,
            fontWeight = FontWeight.Medium,
            color = HexgramColors.goldLight,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Four pillars
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            result.pillars.forEachIndexed { i, p ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = p.label,
                        fontSize = 9.sp,
                        fontFamily = SerifFont,
                        color = HexgramColors.textSecondary
                    )
                    Text(
                        text = p.gan,
                        fontSize = 20.sp,
                        fontFamily = SerifFont,
                        color = if (i == 2) HexgramColors.accent else HexgramColors.goldLight
                    )
                    Text(
                        text = p.zhi,
                        fontSize = 20.sp,
                        fontFamily = SerifFont,
                        color = HexgramColors.gold
                    )
                    Text(
                        text = p.shiShen,
                        fontSize = 9.sp,
                        fontFamily = SerifFont,
                        color = HexgramColors.textSecondary
                    )
                    Text(
                        text = result.naYinPillars.getOrElse(i) { "" },
                        fontSize = 8.sp,
                        fontFamily = SerifFont,
                        color = HexgramColors.textTertiary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = HexgramColors.border)
        Spacer(modifier = Modifier.height(8.dp))

        // Footer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "日主${result.riGan}（${result.riGanWuxing}）${if (result.isStrong) "身旺" else "身弱"}",
                fontSize = 11.sp,
                fontFamily = SerifFont,
                color = HexgramColors.textPrimary
            )
            Text(
                text = "易学三合 · 八字排盘",
                fontSize = 9.sp,
                fontFamily = SerifFont,
                color = HexgramColors.textTertiary
            )
        }
    }
}

@Composable
fun HuangliShareCard(result: HuangliResult) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(HexgramColors.bgPrimary)
            .padding(16.dp)
    ) {
        // Date header
        Text(
            text = "${result.year}年${result.month}月${result.day}日　星期${result.weekDay}",
            fontSize = 16.sp,
            fontFamily = SerifFont,
            color = HexgramColors.goldLight,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "${result.riGan}${result.riZhi}日",
            fontSize = 28.sp,
            fontFamily = SerifFont,
            fontWeight = FontWeight.Medium,
            color = HexgramColors.gold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "${result.nianGan}${result.nianZhi}年　${result.lunarMonth}月　${result.shengXiao}年　${result.jianChu}日　${result.erShiBaXiu}宿",
            fontSize = 10.sp,
            fontFamily = SerifFont,
            color = HexgramColors.textSecondary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = HexgramColors.border)
        Spacer(modifier = Modifier.height(8.dp))

        // Yi
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "宜",
                fontSize = 14.sp,
                fontFamily = SerifFont,
                fontWeight = FontWeight.Medium,
                color = HexgramColors.yiGreen,
                modifier = Modifier.width(24.dp)
            )
            Text(
                text = result.yi.ifBlank { "无特别宜事" },
                fontSize = 12.sp,
                fontFamily = SerifFont,
                color = HexgramColors.textPrimary,
                lineHeight = 20.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Ji
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "忌",
                fontSize = 14.sp,
                fontFamily = SerifFont,
                fontWeight = FontWeight.Medium,
                color = HexgramColors.jiRed,
                modifier = Modifier.width(24.dp)
            )
            Text(
                text = result.ji,
                fontSize = 12.sp,
                fontFamily = SerifFont,
                color = HexgramColors.textPrimary,
                lineHeight = 20.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Directions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            DirectionItem("喜神", result.xiShen)
            DirectionItem("财神", result.caiShen)
            DirectionItem("福神", result.fuShen)
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = HexgramColors.border)
        Spacer(modifier = Modifier.height(8.dp))

        // Footer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "冲${result.chongSha}",
                fontSize = 10.sp,
                fontFamily = SerifFont,
                color = HexgramColors.textSecondary
            )
            Text(
                text = "易学三合 · 每日黄历",
                fontSize = 9.sp,
                fontFamily = SerifFont,
                color = HexgramColors.textTertiary
            )
        }
    }
}

@Composable
private fun DirectionItem(name: String, direction: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = name,
            fontSize = 10.sp,
            fontFamily = SerifFont,
            color = HexgramColors.textSecondary
        )
        Text(
            text = direction,
            fontSize = 12.sp,
            fontFamily = SerifFont,
            color = HexgramColors.gold
        )
    }
}

@Composable
private fun ShareYaoSymbol(
    isYang: Boolean,
    isChanging: Boolean,
    modifier: Modifier = Modifier
) {
    val lineColor = if (isChanging) HexgramColors.accent else HexgramColors.gold
    Canvas(
        modifier = modifier.height(10.dp)
    ) {
        val y = size.height / 2
        val w = size.width
        if (isYang) {
            drawLine(lineColor, Offset(0f, y), Offset(w, y), strokeWidth = 4f, cap = StrokeCap.Round)
        } else {
            val gap = w * 0.16f
            val mid = w / 2
            drawLine(lineColor, Offset(0f, y), Offset(mid - gap / 2, y), strokeWidth = 4f, cap = StrokeCap.Round)
            drawLine(lineColor, Offset(mid + gap / 2, y), Offset(w, y), strokeWidth = 4f, cap = StrokeCap.Round)
        }
    }
}

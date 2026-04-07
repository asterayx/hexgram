package com.hexgram.android.ui.components

import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hexgram.android.ui.theme.HexgramColors
import com.hexgram.android.ui.theme.SerifFont

@Composable
fun SectionHeader(
    subtitle: String,
    title: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = subtitle,
            fontSize = 12.sp,
            fontFamily = SerifFont,
            color = HexgramColors.textSecondary,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            fontSize = 22.sp,
            fontFamily = SerifFont,
            fontWeight = FontWeight.Bold,
            color = HexgramColors.gold,
            letterSpacing = 4.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        // Decorative divider
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(1.dp)
                    .background(HexgramColors.goldDark.copy(alpha = 0.5f))
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(HexgramColors.gold)
            )
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(1.dp)
                    .background(HexgramColors.goldDark.copy(alpha = 0.5f))
            )
        }
    }
}

@Composable
fun PanelCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(HexgramColors.bgPanel)
            .border(1.dp, HexgramColors.border, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        content()
    }
}

@Composable
fun ResultCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(HexgramColors.bgResult)
            .border(1.dp, HexgramColors.border.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        content()
    }
}

@Composable
fun GoldButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = HexgramColors.gold,
            contentColor = HexgramColors.bgPrimary,
            disabledContainerColor = HexgramColors.goldDark.copy(alpha = 0.4f),
            disabledContentColor = HexgramColors.textTertiary
        )
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontFamily = SerifFont,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun GhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(10.dp),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (enabled) HexgramColors.gold else HexgramColors.textTertiary
            )
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = HexgramColors.gold,
            disabledContentColor = HexgramColors.textTertiary
        )
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontFamily = SerifFont,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun LoadingSpinner(
    text: String = "加载中...",
    modifier: Modifier = Modifier
) {
    // Keep screen on while loading
    val view = LocalView.current
    DisposableEffect(Unit) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(36.dp),
            color = HexgramColors.gold,
            strokeWidth = 3.dp,
            trackColor = HexgramColors.border
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            fontFamily = SerifFont,
            color = HexgramColors.textSecondary
        )
    }
}

/**
 * Draws a single yao line:
 * - Yang (unbroken): one solid line
 * - Yin (broken): two segments with gap in middle
 * - Changing yaos are drawn in accent color with a marker
 */
@Composable
fun YaoSymbol(
    isYang: Boolean,
    isChanging: Boolean,
    width: Dp = 120.dp,
    modifier: Modifier = Modifier
) {
    val lineColor = if (isChanging) HexgramColors.dongYao else HexgramColors.gold
    val strokeW = 5f

    Canvas(
        modifier = modifier
            .width(width)
            .height(16.dp)
    ) {
        val y = size.height / 2
        val w = size.width

        if (isYang) {
            // Solid line
            drawLine(
                color = lineColor,
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )
        } else {
            // Two segments with gap
            val gap = w * 0.12f
            val midStart = (w - gap) / 2
            val midEnd = midStart + gap
            drawLine(
                color = lineColor,
                start = Offset(0f, y),
                end = Offset(midStart, y),
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )
            drawLine(
                color = lineColor,
                start = Offset(midEnd, y),
                end = Offset(w, y),
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )
        }

        // Changing marker: small circle
        if (isChanging) {
            drawCircle(
                color = lineColor,
                radius = 4f,
                center = Offset(w + 14f, y)
            )
        }
    }
}

/**
 * Basic markdown text renderer.
 * Supports: ## headers, **bold**, plain text paragraphs.
 */
@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    baseColor: Color = HexgramColors.textPrimary
) {
    val lines = text.split("\n")

    Column(modifier = modifier.fillMaxWidth()) {
        for (line in lines) {
            when {
                line.startsWith("### ") -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = line.removePrefix("### "),
                        fontSize = 16.sp,
                        fontFamily = SerifFont,
                        fontWeight = FontWeight.Bold,
                        color = HexgramColors.goldLight
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                line.startsWith("## ") -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = line.removePrefix("## "),
                        fontSize = 18.sp,
                        fontFamily = SerifFont,
                        fontWeight = FontWeight.Bold,
                        color = HexgramColors.gold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
                line.startsWith("# ") -> {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = line.removePrefix("# "),
                        fontSize = 22.sp,
                        fontFamily = SerifFont,
                        fontWeight = FontWeight.Bold,
                        color = HexgramColors.goldLight
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                line.isBlank() -> {
                    Spacer(modifier = Modifier.height(8.dp))
                }
                else -> {
                    Text(
                        text = parseBoldMarkdown(line),
                        fontSize = 15.sp,
                        fontFamily = SerifFont,
                        color = baseColor,
                        lineHeight = 24.sp
                    )
                }
            }
        }
    }
}

/**
 * Parse **bold** markers within a single line and return AnnotatedString.
 */
private fun parseBoldMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        var remaining = text
        while (remaining.isNotEmpty()) {
            val startIdx = remaining.indexOf("**")
            if (startIdx < 0) {
                append(remaining)
                break
            }
            // Append text before the bold marker
            append(remaining.substring(0, startIdx))
            remaining = remaining.substring(startIdx + 2)

            val endIdx = remaining.indexOf("**")
            if (endIdx < 0) {
                // No closing marker, just append as-is
                append("**")
                append(remaining)
                break
            }
            // Append bold text
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = HexgramColors.goldLight)) {
                append(remaining.substring(0, endIdx))
            }
            remaining = remaining.substring(endIdx + 2)
        }
    }
}

@Composable
fun TagChip(
    text: String,
    color: Color = HexgramColors.gold,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontFamily = SerifFont,
            color = color
        )
    }
}

@Composable
fun ProgressDots(
    total: Int,
    filled: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until total) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(
                        if (i < filled) HexgramColors.gold
                        else HexgramColors.border
                    )
            )
        }
    }
}

@Composable
fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = HexgramColors.textPrimary
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontFamily = SerifFont,
            color = HexgramColors.textSecondary
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontFamily = SerifFont,
            color = valueColor,
            fontWeight = FontWeight.Medium
        )
    }
}

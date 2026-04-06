package com.hexgram.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object HexgramColors {
    val gold = Color(0xFFC9A96E)
    val goldLight = Color(0xFFF5DEB3)
    val goldDark = Color(0xFFA07840)

    val bgPrimary = Color(0xFF0D0B08)
    val bgPanel = Color(0xFF1A1510)
    val bgResult = Color(0xFF13100C)

    val border = Color(0xFF3D3425)
    val textPrimary = Color(0xFFE8DCC8)
    val textSecondary = Color(0xFF8B7355)
    val textTertiary = Color(0xFF5A4D3A)

    val accent = Color(0xFFE8A444)

    val wood = Color(0xFF66BB6A)
    val fire = Color(0xFFEF5350)
    val earth = Color(0xFFFFA726)
    val metal = Color(0xFFE0E0E0)
    val water = Color(0xFF42A5F5)

    val yiGreen = Color(0xFF8BC34A)
    val jiRed = Color(0xFFE57373)

    val dongYao = Color(0xFFE8A444)
}

fun wuxingColor(wx: String): Color = when (wx) {
    "木" -> HexgramColors.wood
    "火" -> HexgramColors.fire
    "土" -> HexgramColors.earth
    "金" -> HexgramColors.metal
    "水" -> HexgramColors.water
    else -> HexgramColors.textSecondary
}

val SerifFont = FontFamily.Serif

private val HexgramColorScheme = darkColorScheme(
    primary = HexgramColors.gold,
    onPrimary = HexgramColors.bgPrimary,
    secondary = HexgramColors.goldDark,
    onSecondary = HexgramColors.textPrimary,
    tertiary = HexgramColors.accent,
    background = HexgramColors.bgPrimary,
    onBackground = HexgramColors.textPrimary,
    surface = HexgramColors.bgPanel,
    onSurface = HexgramColors.textPrimary,
    surfaceVariant = HexgramColors.bgResult,
    onSurfaceVariant = HexgramColors.textSecondary,
    outline = HexgramColors.border,
    error = HexgramColors.jiRed,
)

private val HexgramTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = SerifFont,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        color = HexgramColors.goldLight
    ),
    headlineLarge = TextStyle(
        fontFamily = SerifFont,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        color = HexgramColors.gold
    ),
    headlineMedium = TextStyle(
        fontFamily = SerifFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        color = HexgramColors.gold
    ),
    headlineSmall = TextStyle(
        fontFamily = SerifFont,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        color = HexgramColors.goldLight
    ),
    titleLarge = TextStyle(
        fontFamily = SerifFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        color = HexgramColors.textPrimary
    ),
    titleMedium = TextStyle(
        fontFamily = SerifFont,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        color = HexgramColors.textPrimary
    ),
    bodyLarge = TextStyle(
        fontFamily = SerifFont,
        fontSize = 16.sp,
        color = HexgramColors.textPrimary
    ),
    bodyMedium = TextStyle(
        fontFamily = SerifFont,
        fontSize = 14.sp,
        color = HexgramColors.textPrimary
    ),
    bodySmall = TextStyle(
        fontFamily = SerifFont,
        fontSize = 12.sp,
        color = HexgramColors.textSecondary
    ),
    labelLarge = TextStyle(
        fontFamily = SerifFont,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        color = HexgramColors.textPrimary
    ),
    labelMedium = TextStyle(
        fontFamily = SerifFont,
        fontSize = 12.sp,
        color = HexgramColors.textSecondary
    ),
    labelSmall = TextStyle(
        fontFamily = SerifFont,
        fontSize = 10.sp,
        color = HexgramColors.textTertiary
    ),
)

@Composable
fun HexgramTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = HexgramColorScheme,
        typography = HexgramTypography,
        content = content
    )
}

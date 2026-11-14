package com.joy.sandbox.feature.dungeon.impl.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Локальная тема фичи «Подземелье» — красит Material-компоненты
 * (кнопки, диалоги, текст) в палитру мини-сайта. Типографика как на сайте:
 * заголовки — сериф (аналог Georgia), текст — системный гротеск (system-ui).
 */
private val DungeonColorScheme = darkColorScheme(
    primary = DungeonColors.Gold,
    onPrimary = DungeonColors.OnGold,
    secondary = DungeonColors.GoldBright,
    onSecondary = DungeonColors.OnGold,
    background = DungeonColors.Bg,
    onBackground = DungeonColors.Text,
    surface = DungeonColors.Panel,
    onSurface = DungeonColors.Text,
    surfaceVariant = DungeonColors.ButtonBg,
    onSurfaceVariant = DungeonColors.TextDim,
    outline = DungeonColors.PanelEdge,
    error = DungeonColors.RedBright,
    onError = DungeonColors.Text,
)

private val DungeonTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 44.sp,
        letterSpacing = 2.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 26.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 26.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 20.sp,
    ),
)

@Composable
internal fun DungeonTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DungeonColorScheme,
        typography = DungeonTypography,
        content = content,
    )
}

package com.joy.sandbox.feature.dungeon.impl.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * Локальная тема фичи «Подземелье» — красит Material-компоненты
 * (кнопки, диалоги, текст) в палитру мини-сайта.
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

@Composable
internal fun DungeonTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DungeonColorScheme,
        content = content,
    )
}

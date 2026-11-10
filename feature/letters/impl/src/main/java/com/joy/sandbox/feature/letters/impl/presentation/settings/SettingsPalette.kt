package com.joy.sandbox.feature.letters.impl.presentation.settings

import androidx.compose.ui.graphics.Color
import com.joy.sandbox.core.ui.DesignSystem

/** Палитра экрана настроек: дневной или ночной экран передаёт свою. */
data class SettingsPalette(
    val text: Color,
    val accent: Color,
    val buttonContent: Color,
    val optionTileSelected: Color,
    val optionTileUnselected: Color,
    val switchTrackChecked: Color,
)

/** Дневная палитра настроек. */
val DaySettingsPalette: SettingsPalette
    get() = SettingsPalette(
        text = DesignSystem.Colors.Letters.TextPrimaryDay,
        accent = DesignSystem.Colors.Letters.ButtonContainerDay,
        buttonContent = DesignSystem.Colors.Letters.ButtonContentDay,
        optionTileSelected = DesignSystem.Colors.Letters.OptionTileSelectedDay,
        optionTileUnselected = DesignSystem.Colors.Letters.OptionTileUnselectedDay,
        switchTrackChecked = DesignSystem.Colors.Letters.SwitchTrackCheckedDay,
    )

/** Ночная палитра настроек. */
val NightSettingsPalette: SettingsPalette
    get() = SettingsPalette(
        text = DesignSystem.Colors.Letters.TextPrimaryNight,
        accent = DesignSystem.Colors.Letters.ButtonContainerNight,
        buttonContent = DesignSystem.Colors.Letters.ButtonContentNight,
        optionTileSelected = DesignSystem.Colors.Letters.OptionTileSelectedNight,
        optionTileUnselected = DesignSystem.Colors.Letters.OptionTileUnselectedNight,
        switchTrackChecked = DesignSystem.Colors.Letters.SwitchTrackCheckedNight,
    )

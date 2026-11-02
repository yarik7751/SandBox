package com.joy.sandbox.feature.letters.impl.presentation.settings.model

import androidx.annotation.StringRes

/**
 * Вариант настройки (алфавит или порядок) для отображения RadioButton-группой.
 */
data class SettingsOptionUiModel<T>(
    val value: T,
    @StringRes val labelRes: Int,
    val selected: Boolean,
)

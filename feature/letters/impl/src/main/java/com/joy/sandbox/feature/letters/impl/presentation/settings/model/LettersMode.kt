package com.joy.sandbox.feature.letters.impl.presentation.settings.model

import androidx.annotation.StringRes
import com.joy.sandbox.feature.letters.impl.R

/**
 * Режим показа букв — вкладки экрана настроек. У каждого режима
 * свои настройки и свой источник данных.
 */
enum class LettersMode(@StringRes val labelRes: Int) {

    DAY(R.string.letters_mode_day),
    NIGHT(R.string.letters_mode_night),
}

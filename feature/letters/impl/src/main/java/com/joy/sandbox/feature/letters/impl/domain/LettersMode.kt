package com.joy.sandbox.feature.letters.impl.domain

import androidx.annotation.StringRes
import com.joy.sandbox.feature.letters.impl.R

/**
 * Режим показа букв — вкладки экрана настроек. У каждого режима
 * свои настройки и свой источник данных. Последний выбранный режим
 * сохраняется и восстанавливается при входе в настройки.
 */
enum class LettersMode(@StringRes val labelRes: Int) {

    DAY(R.string.letters_mode_day),
    NIGHT(R.string.letters_mode_night),
}

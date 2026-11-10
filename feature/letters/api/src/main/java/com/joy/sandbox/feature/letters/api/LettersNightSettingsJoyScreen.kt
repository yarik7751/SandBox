package com.joy.sandbox.feature.letters.api

import com.joy.sandbox.core.navigation.JoyScreen
import com.joy.sandbox.core.navigation.NoArgs

/**
 * Ночные настройки фичи «Буквы»: выбор алфавита, порядка показа
 * и длительности таймера. Экран всегда в тёмной палитре.
 * Открываются с главного экрана фичи.
 */
object LettersNightSettingsJoyScreen : JoyScreen<NoArgs> {

    const val ROUTE = "letters_night_settings"
    override val args: NoArgs
        get() = NoArgs

    override val route: String
        get() = ROUTE
}

package com.joy.sandbox.feature.letters.api

import com.joy.sandbox.core.navigation.JoyScreen
import com.joy.sandbox.core.navigation.NoArgs

/**
 * Дневные настройки фичи «Буквы»: выбор алфавита, порядка показа
 * и звука. Открываются с главного экрана фичи.
 */
object LettersDaySettingsJoyScreen : JoyScreen<NoArgs> {

    const val ROUTE = "letters_day_settings"
    override val args: NoArgs
        get() = NoArgs

    override val route: String
        get() = ROUTE
}

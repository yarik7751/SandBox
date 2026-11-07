package com.joy.sandbox.feature.letters.api

import com.joy.sandbox.core.navigation.JoyScreen
import com.joy.sandbox.core.navigation.NoArgs

/**
 * Экран настроек фичи «Буквы»: выбор алфавита и порядка показа.
 */
object LettersSettingsJoyScreen : JoyScreen<NoArgs> {

    const val ROUTE = "letters_settings"
    override val args: NoArgs
        get() = NoArgs

    override val route: String
        get() = ROUTE
}

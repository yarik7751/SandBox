package com.joy.sandbox.feature.letters.api

import com.joy.sandbox.core.navigation.JoyScreen
import com.joy.sandbox.core.navigation.NoArgs

/**
 * Главный экран фичи «Буквы»: кнопки «Буквы» и «Ночник» ведут
 * в настройки соответствующего режима.
 */
object LettersMainJoyScreen : JoyScreen<NoArgs> {

    const val ROUTE = "letters_main"
    override val args: NoArgs
        get() = NoArgs

    override val route: String
        get() = ROUTE
}

package com.joy.sandbox.feature.letters.impl.presentation.letters

import androidx.compose.runtime.Composable
import com.joy.sandbox.core.navigation.JoyScreen
import com.joy.sandbox.core.navigation.ScreenContentProvider
import com.joy.sandbox.core.navigation.router.JoyRouter
import com.joy.sandbox.core.ui.ViewModelFactory
import com.joy.sandbox.feature.letters.api.LettersJoyScreen

class LettersScreenProvider : ScreenContentProvider {

    override val screen: JoyScreen<*> = LettersJoyScreen.DEFAULT

    override val stringArgs: List<String> =
        listOf(LettersJoyScreen.ARG_ALPHABET, LettersJoyScreen.ARG_ORDER)

    @Composable
    override fun Content(viewModelFactory: ViewModelFactory, router: JoyRouter) {
        LettersScreen(viewModelFactory = viewModelFactory, router = router)
    }
}

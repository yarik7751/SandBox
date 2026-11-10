package com.joy.sandbox.feature.letters.impl.presentation.letters

import androidx.compose.runtime.Composable
import com.joy.sandbox.core.navigation.JoyScreen
import com.joy.sandbox.core.navigation.ScreenContentProvider
import com.joy.sandbox.core.navigation.router.JoyRouter
import com.joy.sandbox.core.ui.ViewModelFactory
import com.joy.sandbox.feature.letters.api.NightLettersJoyScreen

class NightLettersScreenProvider : ScreenContentProvider {

    override val screen: JoyScreen<*> = NightLettersJoyScreen.DEFAULT

    override val stringArgs: List<String> = listOf(
        NightLettersJoyScreen.ARG_ALPHABET,
        NightLettersJoyScreen.ARG_ORDER,
        NightLettersJoyScreen.ARG_MINUTES,
    )

    @Composable
    override fun Content(viewModelFactory: ViewModelFactory, router: JoyRouter) {
        NightLettersScreen(viewModelFactory = viewModelFactory, router = router)
    }
}

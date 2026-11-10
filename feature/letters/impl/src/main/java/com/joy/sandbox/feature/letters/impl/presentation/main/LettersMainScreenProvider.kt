package com.joy.sandbox.feature.letters.impl.presentation.main

import androidx.compose.runtime.Composable
import com.joy.sandbox.core.navigation.JoyScreen
import com.joy.sandbox.core.navigation.ScreenContentProvider
import com.joy.sandbox.core.navigation.router.JoyRouter
import com.joy.sandbox.core.ui.ViewModelFactory
import com.joy.sandbox.feature.letters.api.LettersMainJoyScreen

class LettersMainScreenProvider : ScreenContentProvider {

    override val screen: JoyScreen<*> = LettersMainJoyScreen

    @Composable
    override fun Content(viewModelFactory: ViewModelFactory, router: JoyRouter) {
        LettersMainScreen(viewModelFactory = viewModelFactory, router = router)
    }
}

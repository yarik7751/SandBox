package com.joy.sandbox.feature.letters.impl.presentation.settings

import androidx.compose.runtime.Composable
import com.joy.sandbox.core.navigation.JoyScreen
import com.joy.sandbox.core.navigation.ScreenContentProvider
import com.joy.sandbox.core.navigation.router.JoyRouter
import com.joy.sandbox.core.ui.ViewModelFactory
import com.joy.sandbox.feature.letters.api.LettersSettingsJoyScreen

class LettersSettingsScreenProvider : ScreenContentProvider {

    override val screen: JoyScreen<*> = LettersSettingsJoyScreen

    @Composable
    override fun Content(viewModelFactory: ViewModelFactory, router: JoyRouter) {
        LettersSettingsScreen(viewModelFactory = viewModelFactory, router = router)
    }
}

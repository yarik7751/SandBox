package com.joy.sandbox.feature.letters.impl.presentation.settings.day

import androidx.compose.runtime.Composable
import com.joy.sandbox.core.navigation.JoyScreen
import com.joy.sandbox.core.navigation.ScreenContentProvider
import com.joy.sandbox.core.navigation.router.JoyRouter
import com.joy.sandbox.core.ui.ViewModelFactory
import com.joy.sandbox.feature.letters.api.LettersDaySettingsJoyScreen

class LettersDaySettingsScreenProvider : ScreenContentProvider {

    override val screen: JoyScreen<*> = LettersDaySettingsJoyScreen

    @Composable
    override fun Content(viewModelFactory: ViewModelFactory, router: JoyRouter) {
        LettersDaySettingsScreen(viewModelFactory = viewModelFactory, router = router)
    }
}

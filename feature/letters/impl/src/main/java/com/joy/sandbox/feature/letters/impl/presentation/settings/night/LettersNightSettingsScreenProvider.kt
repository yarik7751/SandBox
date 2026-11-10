package com.joy.sandbox.feature.letters.impl.presentation.settings.night

import androidx.compose.runtime.Composable
import com.joy.sandbox.core.navigation.JoyScreen
import com.joy.sandbox.core.navigation.ScreenContentProvider
import com.joy.sandbox.core.navigation.router.JoyRouter
import com.joy.sandbox.core.ui.ViewModelFactory
import com.joy.sandbox.feature.letters.api.LettersNightSettingsJoyScreen

class LettersNightSettingsScreenProvider : ScreenContentProvider {

    override val screen: JoyScreen<*> = LettersNightSettingsJoyScreen

    @Composable
    override fun Content(viewModelFactory: ViewModelFactory, router: JoyRouter) {
        LettersNightSettingsScreen(viewModelFactory = viewModelFactory, router = router)
    }
}

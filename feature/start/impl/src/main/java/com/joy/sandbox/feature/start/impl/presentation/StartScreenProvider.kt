package com.joy.sandbox.feature.start.impl.presentation

import androidx.compose.runtime.Composable
import com.joy.sandbox.core.navigation.JoyScreen
import com.joy.sandbox.core.navigation.ScreenContentProvider
import com.joy.sandbox.core.navigation.router.JoyRouter
import com.joy.sandbox.core.ui.ViewModelFactory
import com.joy.sandbox.feature.start.api.StartJoyScreen

class StartScreenProvider : ScreenContentProvider {

    override val screen: JoyScreen<*> = StartJoyScreen

    @Composable
    override fun Content(viewModelFactory: ViewModelFactory, router: JoyRouter) {
        StartScreen(viewModelFactory = viewModelFactory, router = router)
    }
}

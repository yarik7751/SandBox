package com.joy.sandbox.feature.home.impl.presentation

import androidx.compose.runtime.Composable
import com.joy.sandbox.core.navigation.JoyScreen
import com.joy.sandbox.core.navigation.ScreenContentProvider
import com.joy.sandbox.core.navigation.router.JoyRouter
import com.joy.sandbox.core.ui.ViewModelFactory
import com.joy.sandbox.feature.home.api.HomeJoyScreen

class HomeScreenProvider : ScreenContentProvider {

    override val screen: JoyScreen<*> = HomeJoyScreen

    @Composable
    override fun Content(viewModelFactory: ViewModelFactory, router: JoyRouter) {
        HomeScreen(viewModelFactory = viewModelFactory, router = router)
    }
}

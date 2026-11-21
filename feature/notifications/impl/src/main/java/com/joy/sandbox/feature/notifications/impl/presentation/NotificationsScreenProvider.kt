package com.joy.sandbox.feature.notifications.impl.presentation

import androidx.compose.runtime.Composable
import com.joy.sandbox.core.navigation.JoyScreen
import com.joy.sandbox.core.navigation.ScreenContentProvider
import com.joy.sandbox.core.navigation.router.JoyRouter
import com.joy.sandbox.core.ui.ViewModelFactory
import com.joy.sandbox.feature.notifications.api.NotificationsJoyScreen

class NotificationsScreenProvider : ScreenContentProvider {

    override val screen: JoyScreen<*> = NotificationsJoyScreen

    @Composable
    override fun Content(viewModelFactory: ViewModelFactory, router: JoyRouter) {
        NotificationsScreen(viewModelFactory = viewModelFactory, router = router)
    }
}

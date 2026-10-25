package com.joy.sandbox.feature.websocket.impl

import androidx.compose.runtime.Composable
import com.joy.sandbox.core.navigation.JoyScreen
import com.joy.sandbox.core.navigation.ScreenContentProvider
import com.joy.sandbox.core.navigation.router.JoyRouter
import com.joy.sandbox.core.ui.ViewModelFactory
import com.joy.sandbox.feature.websocket.api.WebSocketJoyScreen

class WebSocketScreenProvider : ScreenContentProvider {

    override val screen: JoyScreen<*> = WebSocketJoyScreen

    @Composable
    override fun Content(viewModelFactory: ViewModelFactory, router: JoyRouter) {
        WebSocketScreen(viewModelFactory = viewModelFactory, router = router)
    }
}

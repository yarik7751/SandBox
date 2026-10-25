package com.joy.sandbox.feature.websocket.api

import com.joy.sandbox.core.navigation.JoyScreen
import com.joy.sandbox.core.navigation.NoArgs

object WebSocketJoyScreen : JoyScreen<NoArgs> {

    const val ROUTE = "websocket"
    override val args: NoArgs
        get() = NoArgs

    override val route: String
        get() = ROUTE
}

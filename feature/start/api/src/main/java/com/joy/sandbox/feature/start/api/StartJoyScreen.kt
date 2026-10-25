package com.joy.sandbox.feature.start.api

import com.joy.sandbox.core.navigation.JoyScreen
import com.joy.sandbox.core.navigation.NoArgs

object StartJoyScreen : JoyScreen<NoArgs> {

    const val ROUTE = "start"
    override val args: NoArgs
        get() = NoArgs

    override val route: String
        get() = ROUTE
}

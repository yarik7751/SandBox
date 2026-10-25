package com.joy.sandbox.feature.home.api

import com.joy.sandbox.core.navigation.JoyScreen
import com.joy.sandbox.core.navigation.NoArgs

object HomeJoyScreen : JoyScreen<NoArgs> {

    const val ROUTE = "home"
    override val args: NoArgs
        get() = NoArgs

    override val route: String
        get() = ROUTE
}

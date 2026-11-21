package com.joy.sandbox.feature.notifications.api

import com.joy.sandbox.core.navigation.JoyScreen
import com.joy.sandbox.core.navigation.NoArgs

object NotificationsJoyScreen : JoyScreen<NoArgs> {

    const val ROUTE = "notifications"
    override val args: NoArgs
        get() = NoArgs

    override val route: String
        get() = ROUTE
}

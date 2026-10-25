package com.joy.sandbox.core.navigation.router

import com.joy.sandbox.core.navigation.JoyScreen
import kotlinx.coroutines.flow.Flow

interface ScreensFlow {

    val screensFlow: Flow<List<JoyScreen<*>>>
}

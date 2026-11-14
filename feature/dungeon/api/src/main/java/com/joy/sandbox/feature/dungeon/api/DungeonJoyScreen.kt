package com.joy.sandbox.feature.dungeon.api

import com.joy.sandbox.core.navigation.JoyScreen
import com.joy.sandbox.core.navigation.NoArgs

/** Титульный экран фичи «Подземелье». */
object DungeonJoyScreen : JoyScreen<NoArgs> {

    const val ROUTE = "dungeon"
    override val args: NoArgs
        get() = NoArgs

    override val route: String
        get() = ROUTE
}

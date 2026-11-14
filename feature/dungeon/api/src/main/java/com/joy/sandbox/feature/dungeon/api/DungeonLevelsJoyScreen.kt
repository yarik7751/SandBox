package com.joy.sandbox.feature.dungeon.api

import com.joy.sandbox.core.navigation.JoyScreen
import com.joy.sandbox.core.navigation.NoArgs

/** Экран выбора уровня подземелья. */
object DungeonLevelsJoyScreen : JoyScreen<NoArgs> {

    const val ROUTE = "dungeon_levels"
    override val args: NoArgs
        get() = NoArgs

    override val route: String
        get() = ROUTE
}

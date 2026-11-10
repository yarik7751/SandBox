package com.joy.sandbox.feature.dungeon.api

import com.joy.sandbox.core.navigation.JoyScreenWithArgs

/** Игровой экран уровня. */
class DungeonGameJoyScreen private constructor(val levelId: Long) : JoyScreenWithArgs() {

    override val route: String get() = "$BASE?$ARG_LEVEL_ID=$levelId"
    override val routePattern: String get() = ROUTE_PATTERN

    companion object {
        const val BASE = "dungeon_game"
        const val ROUTE_PATTERN = "dungeon_game?levelId={levelId}"
        const val ARG_LEVEL_ID = "levelId"

        /** Значение для регистрации провайдера в Dagger-мапе (аргументы не важны). */
        val DEFAULT: DungeonGameJoyScreen = create(INITIAL_LEVEL_ID)

        private const val INITIAL_LEVEL_ID = 1L

        fun create(levelId: Long) = DungeonGameJoyScreen(levelId)
    }
}

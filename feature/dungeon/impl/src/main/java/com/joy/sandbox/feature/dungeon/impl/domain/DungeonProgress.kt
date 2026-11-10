package com.joy.sandbox.feature.dungeon.impl.domain

/** Прогресс игрока: разблокированные уровни, рекорды, настройки. */
data class DungeonProgress(
    val unlockedLevel: Int = 1,
    val bestTurns: Map<Int, Int> = emptyMap(),
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
)

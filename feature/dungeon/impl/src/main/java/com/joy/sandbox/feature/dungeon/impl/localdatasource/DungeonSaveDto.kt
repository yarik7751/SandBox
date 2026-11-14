package com.joy.sandbox.feature.dungeon.impl.localdatasource

/** Снимок сохранения «Подземелья» (DataStore Preferences). */
data class DungeonSaveDto(
    val unlockedLevel: Int,
    val bestTurns: Map<Int, Int>,
    val soundEnabled: Boolean,
    val vibrationEnabled: Boolean,
)

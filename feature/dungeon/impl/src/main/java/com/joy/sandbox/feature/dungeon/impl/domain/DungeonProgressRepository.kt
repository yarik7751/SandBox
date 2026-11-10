package com.joy.sandbox.feature.dungeon.impl.domain

/** Хранение прогресса «Подземелья» (аналог SAVE_KEY из js/game.js). */
interface DungeonProgressRepository {

    suspend fun load(): DungeonProgress

    suspend fun saveUnlocked(level: Int)

    suspend fun saveBest(levelId: Int, turns: Int)

    suspend fun setSoundEnabled(enabled: Boolean)

    suspend fun setVibrationEnabled(enabled: Boolean)
}

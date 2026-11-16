package com.joy.sandbox.feature.dungeon.impl.domain

import com.joy.sandbox.core.dungeon.model.DungeonMechanic
import kotlinx.coroutines.flow.Flow

/** Хранение прогресса «Подземелья» (аналог SAVE_KEY из js/game.js). */
interface DungeonProgressRepository {

    suspend fun load(): DungeonProgress

    /** Изменения прогресса — подписчики видят новое состояние сразу после записи. */
    fun observe(): Flow<DungeonProgress>

    suspend fun saveUnlocked(level: Int)

    suspend fun saveBest(levelId: Int, turns: Int)

    suspend fun setSoundEnabled(enabled: Boolean)

    suspend fun setVibrationEnabled(enabled: Boolean)

    suspend fun markMechanicSeen(mechanic: DungeonMechanic)
}

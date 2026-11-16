package com.joy.sandbox.feature.dungeon.impl.domain

import com.joy.sandbox.core.dungeon.model.DungeonLevel

/**
 * Репозиторий списка уровней: рукотворные уровни ядра (1..5) +
 * сгенерированные из JSON-пака assets. id глобально уникальны и стабильны —
 * прогресс ключуется по id.
 */
interface DungeonLevelsRepository {

    suspend fun levels(): List<DungeonLevel>

    /** Уровень по id; null если такого нет (например, следующего за последним). */
    suspend fun byId(id: Int): DungeonLevel?
}

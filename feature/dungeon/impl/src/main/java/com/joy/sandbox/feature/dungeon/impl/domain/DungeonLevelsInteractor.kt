package com.joy.sandbox.feature.dungeon.impl.domain

import com.joy.sandbox.core.dungeon.model.DungeonLevel
import javax.inject.Inject

/** Доступ к списку уровней для presentation-слоя. */
interface DungeonLevelsInteractor {

    suspend fun levels(): List<DungeonLevel>

    suspend fun byId(id: Int): DungeonLevel?
}

class DungeonLevelsInteractorImpl @Inject constructor(
    private val repository: DungeonLevelsRepository,
) : DungeonLevelsInteractor {

    override suspend fun levels(): List<DungeonLevel> = repository.levels()

    override suspend fun byId(id: Int): DungeonLevel? = repository.byId(id)
}

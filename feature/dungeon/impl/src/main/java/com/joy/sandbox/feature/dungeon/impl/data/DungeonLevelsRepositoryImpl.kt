package com.joy.sandbox.feature.dungeon.impl.data

import com.joy.sandbox.core.dungeon.model.DungeonLevel
import com.joy.sandbox.core.dungeon.model.DungeonLevels
import com.joy.sandbox.feature.dungeon.impl.domain.DungeonLevelsRepository
import com.joy.sandbox.feature.dungeon.impl.localdatasource.DungeonLevelsPackLocalDataSource
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Список уровней: зашитые рукотворные ([DungeonLevels]) + JSON-пак из assets,
 * слитые и отсортированные по id. Список иммутабелен — кэшируется после
 * первой загрузки.
 */
@Singleton
class DungeonLevelsRepositoryImpl @Inject constructor(
    private val packLocalDataSource: DungeonLevelsPackLocalDataSource,
    private val mapper: DungeonLevelsDataMapper,
) : DungeonLevelsRepository {

    private var cache: List<DungeonLevel>? = null

    override suspend fun levels(): List<DungeonLevel> =
        cache ?: load().also { cache = it }

    override suspend fun byId(id: Int): DungeonLevel? =
        levels().firstOrNull { it.id == id }

    private suspend fun load(): List<DungeonLevel> {
        val handmade = DungeonLevels.ALL.map(mapper::toDataModel)
        val packed = packLocalDataSource.loadPack().levels.map(mapper::toDataModel)
        return (handmade + packed)
            .sortedBy { it.id }
            .map(mapper::toDomain)
    }
}

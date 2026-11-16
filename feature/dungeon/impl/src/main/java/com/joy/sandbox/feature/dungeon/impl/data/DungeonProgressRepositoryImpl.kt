package com.joy.sandbox.feature.dungeon.impl.data

import com.joy.sandbox.feature.dungeon.impl.domain.DungeonProgress
import com.joy.sandbox.feature.dungeon.impl.domain.DungeonProgressRepository
import com.joy.sandbox.feature.dungeon.impl.localdatasource.DungeonSaveLocalDataSource
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class DungeonProgressRepositoryImpl @Inject constructor(
    private val localDataSource: DungeonSaveLocalDataSource,
    private val mapper: DungeonProgressDataMapper,
) : DungeonProgressRepository {

    override suspend fun load(): DungeonProgress = mapper.toDomain(localDataSource.load())

    override fun observe(): Flow<DungeonProgress> = localDataSource.observe().map(mapper::toDomain)

    override suspend fun saveUnlocked(level: Int) {
        localDataSource.saveUnlocked(level)
    }

    override suspend fun saveBest(levelId: Int, turns: Int) {
        localDataSource.saveBest(levelId, turns)
    }

    override suspend fun setSoundEnabled(enabled: Boolean) {
        localDataSource.setSoundEnabled(enabled)
    }

    override suspend fun setVibrationEnabled(enabled: Boolean) {
        localDataSource.setVibrationEnabled(enabled)
    }
}

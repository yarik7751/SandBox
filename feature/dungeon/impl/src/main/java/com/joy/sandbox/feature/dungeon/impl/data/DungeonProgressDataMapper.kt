package com.joy.sandbox.feature.dungeon.impl.data

import com.joy.sandbox.feature.dungeon.impl.domain.DungeonProgress
import com.joy.sandbox.feature.dungeon.impl.localdatasource.DungeonSaveDto
import javax.inject.Inject

/** Маппинг сохранения между localdatasource ([DungeonSaveDto]) и domain ([DungeonProgress]). */
class DungeonProgressDataMapper @Inject constructor() {

    fun toDomain(dto: DungeonSaveDto): DungeonProgress = DungeonProgress(
        unlockedLevel = dto.unlockedLevel,
        bestTurns = dto.bestTurns,
        soundEnabled = dto.soundEnabled,
        vibrationEnabled = dto.vibrationEnabled,
    )
}

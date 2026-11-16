package com.joy.sandbox.feature.dungeon.impl.data

import com.joy.sandbox.core.dungeon.model.DungeonMechanic
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
        // Неизвестные ключи (старые/битые данные) игнорируем, чтобы одна запись
        // не ломала чтение всего сохранения.
        seenMechanics = dto.seenMechanics.mapNotNull { id ->
            DungeonMechanic.entries.firstOrNull { it.id == id }
        }.toSet(),
    )
}

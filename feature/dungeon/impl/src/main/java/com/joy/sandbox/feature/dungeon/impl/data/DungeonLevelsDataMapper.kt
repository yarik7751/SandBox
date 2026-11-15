package com.joy.sandbox.feature.dungeon.impl.data

import com.joy.sandbox.core.dungeon.model.DoorId
import com.joy.sandbox.core.dungeon.model.DungeonLevel
import com.joy.sandbox.core.dungeon.model.Pos
import com.joy.sandbox.feature.dungeon.impl.localdatasource.DungeonLevelEntryDto
import javax.inject.Inject

/** Маппинг уровней: паковый DTO и рукотворное ядро → [DungeonLevelDataModel] → доменная модель ядра. */
class DungeonLevelsDataMapper @Inject constructor() {

    fun toDataModel(entry: DungeonLevelEntryDto): DungeonLevelDataModel = DungeonLevelDataModel(
        id = entry.id,
        par = entry.par,
        name = entry.name,
        w = entry.w,
        h = entry.h,
        player = entry.player.toPos(),
        enemy = entry.enemy.toPos(),
        hostages = entry.hostages.map { it.toPos() },
        closed = entry.closed.mapNotNull { DoorId.parse(it) }.toSet(),
        blocked = entry.blocked.map { it.toPos() }.toSet(),
        concealed = entry.concealed.map { it.toPos() }.toSet(),
        jammed = entry.jammed.mapNotNull { DoorId.parse(it) }.toSet(),
        intro = entry.intro,
        togglesBudget = entry.togglesBudget,
        enemyDoorCharge = entry.enemyDoorCharge,
    )

    fun toDataModel(level: DungeonLevel): DungeonLevelDataModel = DungeonLevelDataModel(
        id = level.id,
        par = level.par,
        name = level.name,
        w = level.w,
        h = level.h,
        player = level.player,
        enemy = level.enemy,
        hostages = level.hostages,
        closed = level.closed,
        blocked = level.blocked,
        concealed = level.concealed,
        jammed = level.jammed,
        intro = level.intro,
        togglesBudget = level.togglesBudget,
        enemyDoorCharge = level.enemyDoorCharge,
    )

    /** Доменная модель — ядровая [DungeonLevel]: приложение играет тем, что в ядре. */
    fun toDomain(model: DungeonLevelDataModel): DungeonLevel = DungeonLevel(
        id = model.id,
        par = model.par,
        name = model.name,
        w = model.w,
        h = model.h,
        player = model.player,
        enemy = model.enemy,
        hostages = model.hostages,
        closed = model.closed,
        intro = model.intro,
        blocked = model.blocked,
        concealed = model.concealed,
        jammed = model.jammed,
        togglesBudget = model.togglesBudget,
        enemyDoorCharge = model.enemyDoorCharge,
    )

    private fun Pair<Int, Int>.toPos(): Pos = Pos(first, second)
}

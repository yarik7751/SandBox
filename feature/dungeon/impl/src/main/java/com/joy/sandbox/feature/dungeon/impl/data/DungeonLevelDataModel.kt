package com.joy.sandbox.feature.dungeon.impl.data

import com.joy.sandbox.core.dungeon.model.DoorId
import com.joy.sandbox.core.dungeon.model.Pos

/** Уровень подземелья в data-слое: рукотворные (ядро) и паковые (assets) приводятся к нему. */
data class DungeonLevelDataModel(
    val id: Int,
    val par: Int,
    val name: String,
    val w: Int,
    val h: Int,
    val player: Pos,
    val enemy: Pos,
    val hostages: List<Pos>,
    val closed: Set<DoorId>,
    val blocked: Set<Pos>,
    val concealed: Set<Pos>,
    val jammed: Set<DoorId>,
    val intro: String,
    val togglesBudget: Int,
    val enemyDoorCharge: Int,
    val extraMoves: Int,
)

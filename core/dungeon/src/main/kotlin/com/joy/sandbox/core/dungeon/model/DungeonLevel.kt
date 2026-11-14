package com.joy.sandbox.core.dungeon.model

/** Описание уровня: сетка, стартовые позиции, запертые двери, пар. */
data class DungeonLevel(
    val id: Int,
    val par: Int,
    val name: String,
    val w: Int,
    val h: Int,
    val player: Pos,
    val enemy: Pos,
    val hostages: List<Pos>,
    val closed: Set<DoorId>,
    val intro: String,
)

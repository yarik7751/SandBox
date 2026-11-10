package com.joy.sandbox.core.dungeon.model

/** Состояние пленника. */
data class HostageState(
    val id: Int,
    val room: Pos,
    val freed: Boolean = false,
    val exited: Boolean = false,
    val dead: Boolean = false,
)

/** Исход партии. */
data class GameOver(val result: Result, val reason: String) {

    enum class Result { WIN, LOSE }
}

/**
 * Неизменяемое состояние партии. Все переходы — через [com.joy.sandbox.core.dungeon.DungeonEngine],
 * который возвращает новое состояние и события хода.
 */
data class DungeonState(
    val level: DungeonLevel,
    val openDoors: Set<DoorId>,
    val player: Pos,
    val enemy: Pos,
    val hostages: List<HostageState>,
    val turns: Int = 0,
    val over: GameOver? = null,
    val enemyTrapped: Boolean = false,
    val exitRoom: Pos = Pos(level.w - 1, 0),
)

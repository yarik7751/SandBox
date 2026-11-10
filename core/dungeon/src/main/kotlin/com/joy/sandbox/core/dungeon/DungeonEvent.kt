package com.joy.sandbox.core.dungeon

import com.joy.sandbox.core.dungeon.model.DoorId
import com.joy.sandbox.core.dungeon.model.Pos

/** Кто совершил перемещение / кто погиб. */
enum class Mover { PLAYER, ENEMY, HOSTAGE }

/** События хода — для анимации и звука. Порядок событий значим. */
sealed interface DungeonEvent {

    data class Move(
        val who: Mover,
        val hostageId: Int?,
        val from: Pos,
        val to: Pos,
    ) : DungeonEvent

    data class Free(val hostageId: Int) : DungeonEvent

    data class Exit(val hostageId: Int) : DungeonEvent

    data class Toggle(val door: DoorId, val open: Boolean) : DungeonEvent

    data class Death(val who: Mover, val hostageId: Int? = null) : DungeonEvent

    data object Win : DungeonEvent
}

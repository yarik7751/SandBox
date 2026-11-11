package com.joy.sandbox.core.dungeon

import com.joy.sandbox.core.dungeon.model.CharacterRef
import com.joy.sandbox.core.dungeon.model.DoorId
import com.joy.sandbox.core.dungeon.model.DungeonState
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

    /** Смена активного персонажа — бесплатное действие, звук не обязателен. */
    data class Switch(val to: CharacterRef) : DungeonEvent

    data object Win : DungeonEvent
}

/** Отклонённое действие: состояние без изменений, событий нет. */
internal fun reject(state: DungeonState): ActResult =
    ActResult(ok = false, state = state, events = emptyList())

/** Позиция персонажа: герой — [DungeonState.player], пленник — его комната. */
internal fun positionOf(state: DungeonState, actor: CharacterRef): Pos = when (actor) {
    CharacterRef.Hero -> state.player
    is CharacterRef.Hostage -> state.hostages.first { it.id == actor.id }.room
}

/** Кого показывать в событии перемещения / смерти персонажа. */
internal fun moverOf(actor: CharacterRef): Mover = when (actor) {
    CharacterRef.Hero -> Mover.PLAYER
    is CharacterRef.Hostage -> Mover.HOSTAGE
}

/** id пленника для события или null для героя. */
internal fun hostageIdOf(actor: CharacterRef): Int? = (actor as? CharacterRef.Hostage)?.id

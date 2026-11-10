package com.joy.sandbox.core.dungeon

import com.joy.sandbox.core.dungeon.model.DoorId
import com.joy.sandbox.core.dungeon.model.DungeonLevel
import com.joy.sandbox.core.dungeon.model.DungeonState
import com.joy.sandbox.core.dungeon.model.HostageState
import com.joy.sandbox.core.dungeon.model.Pos

/**
 * Результат действия: признак допустимости, новое состояние и события хода.
 * При отклонённом действии состояние возвращается без изменений.
 */
data class ActResult(
    val ok: Boolean,
    val state: DungeonState,
    val events: List<DungeonEvent>,
)

/**
 * Игровое ядро «Подземелья» — порт js/rules.js. Чистые функции:
 * состояние передаётся явно, побочных эффектов нет. Порядок проверок
 * (смерти до победы, цели врага: игрок → выход → пленники) сохранён.
 */
object DungeonEngine {

    fun create(level: DungeonLevel): DungeonState = DungeonState(
        level = level,
        openDoors = DungeonGeometry.allDoors(level) - level.closed,
        player = level.player,
        enemy = level.enemy,
        hostages = level.hostages.mapIndexed { index, room ->
            HostageState(id = index, room = room)
        },
    )

    fun act(state: DungeonState, action: DungeonAction): ActResult {
        if (state.over != null) return ActResult(ok = false, state = state, events = emptyList())
        return when (action) {
            is DungeonAction.Move -> move(state, action.to)
            is DungeonAction.Toggle -> toggle(state, action.door)
        }
    }

    /** Комнаты, в которые Повелитель может шагнуть на следующем ходу (подсветка угрозы). */
    fun threatRooms(state: DungeonState): Set<Pos> =
        DungeonGeometry.neighbors(state.enemy, state.level, state.openDoors)
            .map { it.first }
            .toSet()

    private fun reject(state: DungeonState): ActResult =
        ActResult(ok = false, state = state, events = emptyList())

    private fun move(state: DungeonState, to: Pos): ActResult {
        val door = DungeonGeometry.doorId(state.player, to)
        if (door == null || door !in state.openDoors) return reject(state)

        val events = mutableListOf<DungeonEvent>()
        val from = state.player
        val moved = state.copy(player = to, turns = state.turns + 1)
        events += DungeonEvent.Move(who = Mover.PLAYER, hostageId = null, from = from, to = to)

        val resolved = resolveAfterPlayerMove(moved, from, events)
        return ActResult(ok = true, state = resolved, events = events)
    }

    /** Освобождение, цепочка пленников, смерти, ход врага и победа. */
    private fun resolveAfterPlayerMove(
        state: DungeonState,
        from: Pos,
        events: MutableList<DungeonEvent>,
    ): DungeonState {
        val freed = freeHostages(state, to = state.player, events)
        val chained = followChain(freed, from, events)
        val dead = DungeonOutcome.checkDeaths(chained, events)
        return if (dead != null) {
            dead
        } else {
            DungeonOutcome.checkWin(DungeonEnemy.turn(chained, events), events)
        }
    }

    private fun freeHostages(
        state: DungeonState,
        to: Pos,
        events: MutableList<DungeonEvent>,
    ): DungeonState = state.copy(
        hostages = state.hostages.map { hostage ->
            if (!hostage.freed && !hostage.exited && hostage.room == to) {
                events += DungeonEvent.Free(hostage.id)
                hostage.copy(freed = true)
            } else {
                hostage
            }
        },
    )

    private fun followChain(
        state: DungeonState,
        from: Pos,
        events: MutableList<DungeonEvent>,
    ): DungeonState {
        var previous = from
        val moved = state.hostages.sortedBy { it.id }.map { hostage ->
            val updated = if (hostage.freed && !hostage.exited) {
                val oldRoom = hostage.room
                val shifted = hostage.copy(room = previous)
                previous = oldRoom
                events += DungeonEvent.Move(
                    who = Mover.HOSTAGE,
                    hostageId = hostage.id,
                    from = oldRoom,
                    to = shifted.room,
                )
                if (shifted.room == state.exitRoom) {
                    events += DungeonEvent.Exit(hostage.id)
                    shifted.copy(exited = true)
                } else {
                    shifted
                }
            } else {
                hostage
            }
            updated
        }
        return state.copy(hostages = moved)
    }

    private fun toggle(state: DungeonState, door: DoorId): ActResult {
        if (door !in DungeonGeometry.adjacentDoors(state.player)) return reject(state)

        val nowOpen = door !in state.openDoors
        val openDoors = if (nowOpen) state.openDoors + door else state.openDoors - door
        val events = mutableListOf<DungeonEvent>(DungeonEvent.Toggle(door = door, open = nowOpen))

        val next = DungeonEnemy.turn(state.copy(openDoors = openDoors, turns = state.turns + 1), events)
        return ActResult(ok = true, state = DungeonOutcome.checkWin(next, events), events = events)
    }
}

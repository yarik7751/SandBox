package com.joy.sandbox.core.dungeon

import com.joy.sandbox.core.dungeon.model.DungeonState
import com.joy.sandbox.core.dungeon.model.Pos

/**
 * Ход Повелителя Тьмы: выбор цели и движение по BFS.
 *
 * Цель — ближайший достижимый живой персонаж: герой (если он в подземелье)
 * плюс освобождённые не вышедшие пленники. Прикованные пленники неуязвимы
 * и не цели; улица вне сетки — враг туда не выходит. Tie-break при равных
 * расстояниях детерминирован: сначала герой, затем пленник с меньшим id.
 */
internal object DungeonEnemy {

    fun turn(state: DungeonState, events: MutableList<DungeonEvent>): DungeonState {
        val nextRoom = nextRoom(state)
        val moved = if (nextRoom != null) {
            events += DungeonEvent.Move(who = Mover.ENEMY, hostageId = null, from = state.enemy, to = nextRoom)
            state.copy(enemy = nextRoom, enemyTrapped = false)
        } else {
            state.copy(enemyTrapped = true)
        }
        return DungeonOutcome.checkDeaths(moved, events) ?: moved
    }

    private fun nextRoom(state: DungeonState): Pos? {
        val distances = DungeonGeometry.bfsDistances(state.enemy, state.level, state.openDoors)
        val target = state.targets()
            .filter { it.room != state.enemy && it.room in distances }
            .minWithOrNull(compareBy<Target> { distances.getValue(it.room) }.thenBy { it.rank })
            ?: return null
        return DungeonGeometry.bfsNext(state.enemy, listOf(target.room), state.level, state.openDoors)
    }

    /** Герой вне улицы (ранг 0), затем освобождённые пленники по возрастанию id. */
    private fun DungeonState.targets(): List<Target> = buildList {
        if (player != street) add(Target(player, rank = 0))
        hostages
            .filter { it.freed && !it.exited }
            .forEach { add(Target(it.room, rank = it.id + 1)) }
    }

    private data class Target(val room: Pos, val rank: Int)
}

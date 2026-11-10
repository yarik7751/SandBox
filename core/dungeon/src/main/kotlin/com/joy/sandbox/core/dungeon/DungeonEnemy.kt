package com.joy.sandbox.core.dungeon

import com.joy.sandbox.core.dungeon.model.DungeonState
import com.joy.sandbox.core.dungeon.model.Pos

/** Ход Повелителя Тьмы: выбор цели (игрок → выход → пленники) и движение по BFS. */
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

    /** Цель врага по порядку: игрок → выход → неосвобождённые пленники. */
    private fun nextRoom(state: DungeonState): Pos? {
        val toPlayer = bfsTowards(state, listOf(state.player))
        val toExit = toPlayer ?: bfsTowards(state, listOf(state.exitRoom))
        return toExit ?: unfreedTarget(state)
    }

    private fun unfreedTarget(state: DungeonState): Pos? {
        val unfreed = state.hostages.filter { !it.freed && !it.exited }.map { it.room }
        return unfreed.takeIf { it.isNotEmpty() }?.let { bfsTowards(state, it) }
    }

    private fun bfsTowards(state: DungeonState, targets: List<Pos>): Pos? =
        DungeonGeometry.bfsNext(state.enemy, targets, state.level, state.openDoors)
}

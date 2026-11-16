package com.joy.sandbox.core.dungeon

import com.joy.sandbox.core.dungeon.model.DoorId
import com.joy.sandbox.core.dungeon.model.DungeonState
import com.joy.sandbox.core.dungeon.model.Pos

/**
 * Ход Повелителя Тьмы: выбор цели и движение по BFS.
 *
 * Цель — ближайший достижимый живой персонаж: герой (если он в подземелье)
 * плюс освобождённые не вышедшие пленники. Прикованные пленники неуязвимы
 * и не цели; улица вне сетки — враг туда не выходит. Персонажи на тёмных
 * клетках-укрытиях невидимы: не цели, и сам враг в укрытия не входит.
 * Tie-break при равных расстояниях детерминирован: сначала герой, затем
 * пленник с меньшим id.
 *
 * Заряд ([DungeonState.enemyChargesLeft]): оказавшись в ловушке (пути к
 * целям нет), враг тратит заряд и выломывает соседнюю закрытую дверь,
 * дающую путь к цели; перебор соседних дверей — в порядке DIRS.
 */
internal object DungeonEnemy {

    fun turn(state: DungeonState, events: MutableList<DungeonEvent>): DungeonState {
        val nextRoom = nextRoom(state)
        val moved = if (nextRoom != null) {
            events += DungeonEvent.Move(who = Mover.ENEMY, hostageId = null, from = state.enemy, to = nextRoom)
            state.copy(enemy = nextRoom, enemyTrapped = false)
        } else {
            val forcedDoor = doorToForceOpen(state)
            if (forcedDoor != null) {
                events += DungeonEvent.EnemyDoorOpen(forcedDoor)
                state.copy(
                    openDoors = state.openDoors + forcedDoor,
                    enemyTrapped = false,
                    enemyChargesLeft = state.enemyChargesLeft - 1,
                )
            } else {
                state.copy(enemyTrapped = true)
            }
        }
        return DungeonOutcome.checkDeaths(moved, events) ?: moved
    }

    /**
     * Дверь для вылома: враг в ловушке, заряд есть — перебирает соседние
     * закрытые двери (реальные, не заклинившие) в порядке DIRS и берёт
     * первую, после открытия которой к какой-то цели появляется путь.
     */
    private fun doorToForceOpen(state: DungeonState): DoorId? {
        val targets = state.targets().map { it.room }
        val realDoors = DungeonGeometry.allDoors(state.level)
        return if (state.enemyChargesLeft > 0 && targets.isNotEmpty()) {
            DungeonGeometry.adjacentDoors(state.enemy)
                .filter { it in realDoors && it !in state.openDoors }
                .firstOrNull { door -> pathAppearsWith(state, door, targets) }
        } else {
            null
        }
    }

    private fun pathAppearsWith(state: DungeonState, door: DoorId, targets: List<Pos>): Boolean {
        val distances = DungeonGeometry.bfsDistances(
            state.enemy,
            state.level,
            state.openDoors + door,
            forEnemy = true,
        )
        return targets.any { it in distances }
    }

    private fun nextRoom(state: DungeonState): Pos? {
        val distances = DungeonGeometry.bfsDistances(state.enemy, state.level, state.openDoors, forEnemy = true)
        val target = state.targets()
            .filter { it.room != state.enemy && it.room in distances }
            .minWithOrNull(compareBy<Target> { distances.getValue(it.room) }.thenBy { it.rank })
            ?: return null
        return DungeonGeometry.bfsNext(
            state.enemy,
            listOf(target.room),
            state.level,
            state.openDoors,
            forEnemy = true,
        )
    }

    /** Герой вне улицы (ранг 0), затем освобождённые пленники по возрастанию id. */
    private fun DungeonState.targets(): List<Target> = buildList {
        if (player != street && player !in level.concealed) add(Target(player, rank = 0))
        hostages
            .filter { it.freed && !it.exited && it.room !in level.concealed }
            .forEach { add(Target(it.room, rank = it.id + 1)) }
    }

    private data class Target(val room: Pos, val rank: Int)
}

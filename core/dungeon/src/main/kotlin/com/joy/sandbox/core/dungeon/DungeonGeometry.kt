package com.joy.sandbox.core.dungeon

import com.joy.sandbox.core.dungeon.model.DoorId
import com.joy.sandbox.core.dungeon.model.DungeonLevel
import com.joy.sandbox.core.dungeon.model.Pos
import java.util.ArrayDeque

/**
 * Геометрия подземелья и BFS — порт js/rules.js.
 * Порядок направлений важен для детерминизма: up, left, right, down.
 */
object DungeonGeometry {

    val DIRS: List<Pos> = listOf(
        Pos(0, -1), // up
        Pos(-1, 0), // left
        Pos(1, 0), // right
        Pos(0, 1), // down
    )

    fun adjacent(a: Pos, b: Pos): Boolean = kotlin.math.abs(a.x - b.x) + kotlin.math.abs(a.y - b.y) == 1

    /** id двери между двумя соседними комнатами, null если комнаты не соседние. */
    fun doorId(a: Pos, b: Pos): DoorId? = when {
        a.x == b.x && a.y + 1 == b.y -> DoorId.vertical(a.x, a.y)
        a.x == b.x && a.y == b.y + 1 -> DoorId.vertical(b.x, b.y)
        a.x + 1 == b.x && a.y == b.y -> DoorId.horizontal(a.x, a.y)
        a.x == b.x + 1 && a.y == b.y -> DoorId.horizontal(b.x, b.y)
        else -> null
    }

    /** Все двери сетки: горизонтальные 'h:x,y', затем вертикальные 'v:x,y'. */
    fun allDoors(level: DungeonLevel): Set<DoorId> {
        val doors = mutableSetOf<DoorId>()
        for (y in 0 until level.h) {
            for (x in 0 until level.w - 1) doors += DoorId.horizontal(x, y)
        }
        for (y in 0 until level.h - 1) {
            for (x in 0 until level.w) doors += DoorId.vertical(x, y)
        }
        return doors
    }

    /**
     * Двери, примыкающие к комнате [room] — как в JS: по каждому направлению,
     * без проверки границ (несуществующие двери за краем сетки дают отдельный id,
     * их переключение допустимо и тратит ход, но ни на что не влияет).
     */
    fun adjacentDoors(room: Pos): List<DoorId> =
        DIRS.mapNotNull { doorId(room, Pos(room.x + it.x, room.y + it.y)) }

    fun isInside(pos: Pos, level: DungeonLevel): Boolean =
        pos.x in 0 until level.w && pos.y in 0 until level.h

    /** Соседние комнаты, reachable через открытые двери. */
    fun neighbors(room: Pos, level: DungeonLevel, openDoors: Set<DoorId>): List<Pair<Pos, DoorId>> {
        val result = mutableListOf<Pair<Pos, DoorId>>()
        for (dir in DIRS) {
            val door = openDoorTowards(room, dir, level, openDoors)
            if (door != null) result += door
        }
        return result
    }

    private fun openDoorTowards(
        room: Pos,
        dir: Pos,
        level: DungeonLevel,
        openDoors: Set<DoorId>,
    ): Pair<Pos, DoorId>? {
        val next = Pos(room.x + dir.x, room.y + dir.y)
        val door = if (isInside(next, level)) doorId(room, next) else null
        return door?.takeIf { it in openDoors }?.let { next to it }
    }

    /**
     * Следующая комната на кратчайшем пути из [start] к ближайшей из [targets]
     * (BFS, порядок обхода — детерминирован по DIRS). null, если пути нет.
     */
    fun bfsNext(
        start: Pos,
        targets: List<Pos>,
        level: DungeonLevel,
        openDoors: Set<DoorId>,
    ): Pos? {
        val targetSet = targets.toSet()
        if (targetSet.isEmpty() || start in targetSet) return null

        val previous = HashMap<Pos, Pair<Pos, Pos>>() // комната -> (откуда пришли, сама комната)
        val seen = HashSet<Pos>()
        val queue = ArrayDeque<Pos>()
        seen += start
        queue.add(start)

        var found: Pos? = null
        while (queue.isNotEmpty() && found == null) {
            val current = queue.removeFirst()
            found = scanNeighbors(current, targetSet, level, openDoors, seen, previous, queue)
        }
        return found?.let { firstStep(previous, start, it) }
    }

    private fun scanNeighbors(
        current: Pos,
        targetSet: Set<Pos>,
        level: DungeonLevel,
        openDoors: Set<DoorId>,
        seen: MutableSet<Pos>,
        previous: MutableMap<Pos, Pair<Pos, Pos>>,
        queue: ArrayDeque<Pos>,
    ): Pos? {
        for ((next, _) in neighbors(current, level, openDoors)) {
            if (seen.add(next)) {
                previous[next] = current to next
                if (next in targetSet) return next
                queue.add(next)
            }
        }
        return null
    }

    private fun firstStep(previous: Map<Pos, Pair<Pos, Pos>>, start: Pos, found: Pos): Pos {
        var node = previous.getValue(found)
        while (node.first != start) {
            node = previous.getValue(node.first)
        }
        return node.second
    }
}

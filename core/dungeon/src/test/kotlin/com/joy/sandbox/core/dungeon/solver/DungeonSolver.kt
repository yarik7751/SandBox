package com.joy.sandbox.core.dungeon.solver

import com.joy.sandbox.core.dungeon.DungeonAction
import com.joy.sandbox.core.dungeon.DungeonEngine
import com.joy.sandbox.core.dungeon.DungeonGeometry
import com.joy.sandbox.core.dungeon.model.CharacterRef
import com.joy.sandbox.core.dungeon.model.DungeonLevel
import com.joy.sandbox.core.dungeon.model.DungeonState
import com.joy.sandbox.core.dungeon.model.GameOver
import com.joy.sandbox.core.dungeon.model.HostageState
import com.joy.sandbox.core.dungeon.model.Pos
import java.util.PriorityQueue

/**
 * Мультиагентный A* по пространству состояний «Подземелья».
 *
 * Состояние — полный снимок партии (герой, враг, пленники, двери, активный
 * персонаж); переходы — все допустимые действия. Ходы (перемещение, дверь)
 * стоят 1, переключения персонажа и первый бесплатный вход с улицы — 0
 * (стоимость берётся из разницы [DungeonState.turns]).
 *
 * Эвристика допустимая: расстояния считаются по сетке со всеми открытыми
 * дверями — минимально возможные, значит, не превышают любой реальный путь
 * при любой конфигурации дверей. Каждый пленник должен пройти до выхода
 * не менее dist(пленник, выход) ходов, а герой (если он в подземелье) —
 * не менее dist(герой, выход) + 1 хода до улицы; ходы персонажей не
 * пересекаются, поэтому берётся сумма. Отдельно: кто-то должен дойти
 * до каждого прикованного пленника — берётся максимум двух слагаемых.
 * Цель проверяется при извлечении из очереди; с весом 1 решение оптимально
 * по ходам, с большим весом — находится быстрее, но может иметь запас.
 */
object DungeonSolver {

    /** Результат поиска: последовательность действий, число ходов, раскрытия. */
    data class Solution(val actions: List<DungeonAction>, val turns: Int, val expansions: Int)

    /**
     * Ищет решение уровня. Взвешенный A* (f = g + [weight]·h): при весе 1 —
     * оптимум по ходам, при большем весе — заметно быстрее, возможен небольшой
     * запас над оптимумом (для пар-значений это допустимо). null, если решения
     * нет или поиск не уложился в [maxExpansions] раскрытий.
     */
    fun solve(
        level: DungeonLevel,
        weight: Int = WEIGHT,
        maxExpansions: Int = MAX_EXPANSIONS,
    ): Solution? {
        val start = DungeonEngine.create(level)
        val startKey = start.key()
        val distances = openDistances(level)
        val gScore = HashMap<Key, Int>().apply { put(startKey, 0) }
        val cameFrom = HashMap<Key, Pair<Key, DungeonAction>>()
        val open = PriorityQueue(compareBy(Entry::fScore))
        open += Entry(weight * heuristic(distances, start), 0, startKey, start)

        var expansions = 0
        var solution: Solution? = null
        while (open.isNotEmpty() && solution == null) {
            val current = open.poll()
            when {
                // устаревшая запись: g улучшился после помещения в очередь
                current.gScore != gScore[current.key] -> Unit
                current.state.over?.result == GameOver.Result.WIN ->
                    solution = Solution(reconstruct(cameFrom, startKey, current.key), current.gScore, expansions)
                // раскрываем узел, пока не исчерпан бюджет; иначе — стоп поиска
                ++expansions <= maxExpansions ->
                    relaxNeighbors(current, gScore, cameFrom, open, distances, weight)
                else -> open.clear()
            }
        }
        return solution
    }

    /** Раскрытие узла: применяет все допустимые действия, улучшения — в очередь. */
    private fun relaxNeighbors(
        current: Entry,
        gScore: HashMap<Key, Int>,
        cameFrom: HashMap<Key, Pair<Key, DungeonAction>>,
        open: PriorityQueue<Entry>,
        distances: Map<Pos, Map<Pos, Int>>,
        weight: Int,
    ) {
        for (action in actions(current.state)) {
            val result = DungeonEngine.act(current.state, action)
            val next = result.state
            if (result.ok && next.over?.result != GameOver.Result.LOSE) {
                val nextKey = next.key()
                val candidate = current.gScore + (next.turns - current.state.turns)
                if (candidate < gScore.getOrDefault(nextKey, Int.MAX_VALUE)) {
                    gScore[nextKey] = candidate
                    cameFrom[nextKey] = current.key to action
                    open += Entry(
                        candidate + weight * heuristic(distances, next),
                        candidate,
                        nextKey,
                        next,
                    )
                }
            }
        }
    }

    private fun reconstruct(
        cameFrom: Map<Key, Pair<Key, DungeonAction>>,
        startKey: Key,
        goalKey: Key,
    ): List<DungeonAction> {
        val path = mutableListOf<DungeonAction>()
        var cursor = goalKey
        while (cursor != startKey) {
            val (previous, action) = cameFrom.getValue(cursor)
            path += action
            cursor = previous
        }
        return path.asReversed()
    }

    /** Все допустимые действия из состояния: ходы активного, двери героя, переключения. */
    private fun actions(state: DungeonState): List<DungeonAction> = buildList {
        when (val active = state.active) {
            CharacterRef.Hero -> {
                val from = state.player
                if (from == state.street) {
                    add(DungeonAction.Move(state.exitRoom))
                } else {
                    if (from == state.exitRoom) add(DungeonAction.Move(state.street))
                    DungeonGeometry.neighbors(from, state.level, state.openDoors)
                        .forEach { add(DungeonAction.Move(it.first)) }
                    // только реальные двери сетки: «фантомные» за краем (пережиток
                    // js/rules.js) нельзя переключить тапом по доске
                    val realDoors = DungeonGeometry.allDoors(state.level)
                    DungeonGeometry.adjacentDoors(from)
                        .filter { it in realDoors }
                        .forEach { add(DungeonAction.Toggle(it)) }
                }
            }
            is CharacterRef.Hostage -> {
                val from = state.hostages.first { it.id == active.id }.room
                DungeonGeometry.neighbors(from, state.level, state.openDoors)
                    .forEach { add(DungeonAction.Move(it.first)) }
            }
        }
        if (state.active != CharacterRef.Hero) add(DungeonAction.Switch(CharacterRef.Hero))
        state.hostages
            .filter { it.freed && !it.exited && CharacterRef.Hostage(it.id) != state.active }
            .forEach { add(DungeonAction.Switch(CharacterRef.Hostage(it.id))) }
    }

    /** Расстояния по сетке со всеми открытыми дверями — нижняя граница путей
     *  при любой конфигурации дверей. Считается один раз на уровень. */
    private fun openDistances(level: DungeonLevel): Map<Pos, Map<Pos, Int>> {
        val allOpen = DungeonGeometry.allDoors(level)
        val rooms = (0 until level.h).flatMap { y ->
            (0 until level.w).map { x -> Pos(x, y) }
        }
        return rooms.associateWith { DungeonGeometry.bfsDistances(it, level, allOpen) }
    }

    /** Допустимая эвристика по открытой сетке: сумма путей пленников до выхода
     *  плюс выход героя, отдельно — доход до прикованных. Берётся максимум. */
    private fun heuristic(distances: Map<Pos, Map<Pos, Int>>, state: DungeonState): Int {
        fun dist(from: Pos, to: Pos): Int = distances[from]?.get(to) ?: 0

        var walks = 0
        state.hostages.filter { !it.exited }.forEach { hostage ->
            walks += dist(hostage.room, state.exitRoom)
        }
        if (state.player != state.street) {
            walks += dist(state.player, state.exitRoom) + 1
        }
        val reach = state.hostages.filter { !it.freed && !it.exited }.map { chained ->
            val candidates = mutableListOf<Int>()
            if (state.player != state.street) candidates += dist(state.player, chained.room)
            state.hostages.filter { it.freed && !it.exited }.forEach { freer ->
                candidates += dist(freer.room, chained.room)
            }
            candidates.minOrNull() ?: 0
        }.maxOrNull() ?: 0
        return maxOf(walks, reach)
    }

    /** Ключ состояния для посещённых множеств: без turns/over/enemyTrapped.
     *
     *  Двери входят в ключ не напрямую, а через разбиение комнат на области
     *  связности (union-find по открытым дверям): всё поведение игры —
     *  перемещения, BFS врага, угрозы — определяется достижимостью, а она
     *  совпадает при разных наборах дверей с одним разбиением. Переключение
     *  двери-моста между областями даёт новое разбиение, переключение двери
     *  внутри области — тот же ключ (холостые переключения отсекаются сами). */
    private data class Key(
        val player: Pos,
        val enemy: Pos,
        val hostages: List<HostageState>,
        val regions: List<Int>,
        val active: CharacterRef,
    )

    private data class Entry(
        val fScore: Int,
        val gScore: Int,
        val key: Key,
        val state: DungeonState,
    )

    private fun DungeonState.key(): Key = Key(
        player = player,
        enemy = enemy,
        hostages = hostages,
        regions = connectivityRegions(),
        active = active,
    )

    /** Разбиение комнат сетки на области связности по открытым дверям. */
    private fun DungeonState.connectivityRegions(): List<Int> {
        val areas = UnionFind(level.w * level.h)
        for (door in openDoors) {
            val first = Pos(door.x, door.y)
            val second = if (door.horizontal) Pos(door.x + 1, door.y) else Pos(door.x, door.y + 1)
            // «фантомные» двери за краем сетки (пережиток js/rules.js) пропускаем
            if (DungeonGeometry.isInside(first, level) && DungeonGeometry.isInside(second, level)) {
                areas.union(second.y * level.w + second.x, first.y * level.w + first.x)
            }
        }
        // каноническая нумерация: по порядку комнат сетки
        val rootToRegion = HashMap<Int, Int>()
        var nextRegion = 0
        return (0 until areas.size).map { index ->
            rootToRegion.getOrPut(areas.find(index)) { nextRegion++ }
        }
    }

    /** Union-find с сжатием путей для разбиения комнат на области связности. */
    private class UnionFind(val size: Int) {

        private val parent = IntArray(size) { it }

        fun find(index: Int): Int {
            var root = index
            while (parent[root] != root) root = parent[root]
            var cursor = index
            while (parent[cursor] != root) {
                val next = parent[cursor]
                parent[cursor] = root
                cursor = next
            }
            return root
        }

        fun union(child: Int, root: Int) {
            val childRoot = find(child)
            val rootRoot = find(root)
            if (childRoot != rootRoot) parent[rootRoot] = childRoot
        }
    }

    private const val WEIGHT = 5
    private const val MAX_EXPANSIONS = 1_000_000
}

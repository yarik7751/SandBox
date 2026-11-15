package com.joy.sandbox.core.dungeon.generator

import com.joy.sandbox.core.dungeon.model.DoorId
import com.joy.sandbox.core.dungeon.model.DungeonLevel
import com.joy.sandbox.core.dungeon.model.Pos
import kotlin.random.Random

/**
 * Детерминированный генератор уровней (docs/dungeon-generation.md): уровень
 * N однозначно воспроизводится из сида f(N, [attempt]). Прогрессия — наклон,
 * а не тиры: параметры растут непрерывно с индексом, соседние уровни
 * отличаются и раскладкой (сид), и правилами (наклон параметров).
 *
 * Генератор лишь строит кандидата. Отбор решаемых кандидатов solver'ом
 * и запись JSON-пака — в JVM-тестах (см. DungeonPackGeneratorTest):
 * [attempt] — соль перебора: при неудаче solver'а вызывающий код пробует
 * attempt 1, 2, ... — первый решаемый вариант и попадает в пак.
 */
object LevelGenerator {

    fun generate(n: Int, attempt: Int = 0): DungeonLevel {
        require(n >= 1) { "n должно быть >= 1, получено $n" }
        require(attempt >= 0) { "attempt должен быть >= 0, получено $attempt" }
        val random = Random(seed(n, attempt))

        val size = gridSize(n)
        val w = size
        val h = size
        val exitRoom = Pos(w - 1, 0)

        // Завалы не пересекаются с выходом (валидация в DungeonEngine.create).
        val blocked = (rooms(w, h) - exitRoom)
            .shuffled(random)
            .take(withJitter(random, blockedCount(n)))
            .toSet()

        val freeRooms = rooms(w, h) - blocked - exitRoom
        val concealed = freeRooms
            .shuffled(random)
            .take(withJitter(random, concealedCount(n)))
            .toSet()

        // Старты не в укрытиях: враг туда всё равно не входит, но старт
        // во тьме — мёртвый уровень, отбраковывается solver'ом впустую.
        val spawnRooms = freeRooms - concealed
        val doors = allGridDoors(w, h)
        val jammed = doors
            .shuffled(random)
            .take(withJitter(random, jammedCount(n)))
            .toSet()
        val closed = (doors - jammed)
            .shuffled(random)
            .take(withJitter(random, closedCount(n)))
            .toSet()

        val enemy = spawnRooms.shuffled(random).first()
        val hostageCount = if (n >= SECOND_HOSTAGE_FROM) 2 else 1
        val hostages = (spawnRooms - enemy).shuffled(random).take(hostageCount)

        return DungeonLevel(
            id = n,
            par = 0, // пар заполняется solver'ом при сборке пака
            name = "Глубина $n",
            w = w,
            h = h,
            player = exitRoom, // вход совпадает с выходом, как у рукотворных уровней
            enemy = enemy,
            hostages = hostages,
            closed = closed,
            intro = "",
            blocked = blocked,
            concealed = concealed,
            jammed = jammed,
        )
    }

    /** Размер сетки: 4×4 до ~10, 5×5 до ~25, далее 6×6. */
    private fun gridSize(n: Int): Int = when {
        n <= GRID_4_MAX -> GRID_SIZE_SMALL
        n <= GRID_5_MAX -> GRID_SIZE_MEDIUM
        else -> GRID_SIZE_LARGE
    }

    /** Заклиневшие двери: с ~6-го уровня, +1 каждые ~4. */
    private fun jammedCount(n: Int): Int =
        if (n < JAMMED_FROM) 0 else minOf(1 + (n - JAMMED_FROM) / JAMMED_EVERY, JAMMED_MAX)

    /** Завалы: с ~15-го уровня, +1 каждые ~5. */
    private fun blockedCount(n: Int): Int =
        if (n < BLOCKED_FROM) 0 else minOf(1 + (n - BLOCKED_FROM) / BLOCKED_EVERY, BLOCKED_MAX)

    /** Укрытия: с ~30-го уровня, +1 каждые ~6. */
    private fun concealedCount(n: Int): Int =
        if (n < CONCEALED_FROM) 0 else minOf(1 + (n - CONCEALED_FROM) / CONCEALED_EVERY, CONCEALED_MAX)

    /** Изначально закрытые двери: лёгкий наклон поверх базовой механики. */
    private fun closedCount(n: Int): Int = minOf(n / CLOSED_EVERY, CLOSED_MAX)

    private fun withJitter(random: Random, base: Int): Int =
        (base + random.nextInt(from = JITTER_MIN, until = JITTER_MAX_EXCLUSIVE)).coerceAtLeast(0)

    private fun seed(n: Int, attempt: Int): Long =
        n.toLong() * SEED_N_MULTIPLIER + attempt.toLong() * SEED_ATTEMPT_MULTIPLIER + SEED_SALT

    private fun rooms(w: Int, h: Int): Set<Pos> =
        (0 until h).flatMap { y -> (0 until w).map { x -> Pos(x, y) } }.toSet()

    private fun allGridDoors(w: Int, h: Int): Set<DoorId> {
        val doors = mutableSetOf<DoorId>()
        for (y in 0 until h) {
            for (x in 0 until w - 1) doors += DoorId.horizontal(x, y)
        }
        for (y in 0 until h - 1) {
            for (x in 0 until w) doors += DoorId.vertical(x, y)
        }
        return doors
    }

    // Прогрессия — наклон, а не тиры (docs/dungeon-generation.md); точные
    // коэффициенты — черновые, подбираются по данным solver'а.
    private const val GRID_4_MAX = 9
    private const val GRID_5_MAX = 24
    private const val GRID_SIZE_SMALL = 4
    private const val GRID_SIZE_MEDIUM = 5
    private const val GRID_SIZE_LARGE = 6
    private const val JAMMED_FROM = 6
    private const val JAMMED_EVERY = 4
    private const val JAMMED_MAX = 5
    private const val BLOCKED_FROM = 15
    private const val BLOCKED_EVERY = 5
    private const val BLOCKED_MAX = 4
    private const val CONCEALED_FROM = 30
    private const val CONCEALED_EVERY = 6
    private const val CONCEALED_MAX = 3
    private const val CLOSED_EVERY = 8
    private const val CLOSED_MAX = 3
    private const val SECOND_HOSTAGE_FROM = 30
    private const val JITTER_MIN = -1
    private const val JITTER_MAX_EXCLUSIVE = 2
    private const val SEED_N_MULTIPLIER = 1_000_003L
    private const val SEED_ATTEMPT_MULTIPLIER = 97_409L
    private const val SEED_SALT = 0x5DEECE66DL
}

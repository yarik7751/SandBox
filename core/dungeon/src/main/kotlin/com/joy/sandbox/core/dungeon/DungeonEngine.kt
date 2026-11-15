package com.joy.sandbox.core.dungeon

import com.joy.sandbox.core.dungeon.model.CharacterRef
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
 * Игровое ядро «Подземелья» — порт js/rules.js с доработками канона:
 * улица над выходом и ручное управление освобождёнными пленниками
 * (цепочки следования нет). Чистые функции: состояние передаётся явно,
 * побочных эффектов нет. Порядок проверок (смерти до победы) сохранён.
 *
 * Правила улицы: герой стартует на улице, первый вход в подземелье —
 * не ход; пленник, дошедший до выхода, автоматически выходит на улицу;
 * герой может выйти на улицу, только если хотя бы один пленник уже вышел;
 * Повелитель Тьмы на улицу не выходит и атаковать там не может.
 *
 * Правила управления: ход делает активный персонаж (герой или освобождённый
 * пленник); переключение активного — бесплатное действие, не ход; двери
 * переключает только герой; прикованный пленник неуязвим для тьмы.
 * Дополнительный ход — перемещение активного персонажа без ответа врага,
 * тратит запас [com.joy.sandbox.core.dungeon.model.DungeonLevel.extraMoves].
 */
object DungeonEngine {

    fun create(level: DungeonLevel): DungeonState {
        // Валидация раскладки: выход и стартовые позиции не должны оказаться в завале.
        val starts = buildList {
            add(Pos(level.w - 1, 0)) // комната выхода
            add(level.player)
            add(level.enemy)
            addAll(level.hostages)
        }
        require(starts.none { it in level.blocked }) {
            "выход и стартовые позиции не должны быть в завале: " +
                starts.filter { it in level.blocked }
        }
        return DungeonState(
            level = level,
            openDoors = DungeonGeometry.allDoors(level) - level.closed,
            player = Pos(level.w - 1, -1),
            enemy = level.enemy,
            hostages = level.hostages.mapIndexed { index, room ->
                HostageState(id = index, room = room)
            },
        )
    }

    fun act(state: DungeonState, action: DungeonAction): ActResult {
        if (state.over != null) return ActResult(ok = false, state = state, events = emptyList())
        return when (action) {
            is DungeonAction.Move -> step(state, action.to, extra = false)
            is DungeonAction.ExtraMove -> if (state.extrasLeft > 0) {
                step(state, action.to, extra = true)
            } else {
                reject(state)
            }
            is DungeonAction.Toggle -> toggle(state, action.door)
            is DungeonAction.Switch -> switch(state, action.to)
        }
    }

    /** Комнаты, в которые Повелитель может шагнуть на следующем ходу (подсветка угрозы). */
    fun threatRooms(state: DungeonState): Set<Pos> =
        DungeonGeometry.neighbors(state.enemy, state.level, state.openDoors, forEnemy = true)
            .map { it.first }
            .toSet()

    private fun switch(state: DungeonState, to: CharacterRef): ActResult {
        val available = when (to) {
            CharacterRef.Hero -> to != state.active
            is CharacterRef.Hostage -> to != state.active && state.hostages.any {
                it.id == to.id && it.freed && !it.exited
            }
        }
        return if (available) {
            ActResult(
                ok = true,
                state = state.copy(active = to),
                events = listOf(DungeonEvent.Switch(to)),
            )
        } else {
            reject(state)
        }
    }

    private fun step(state: DungeonState, to: Pos, extra: Boolean): ActResult {
        val actor = state.active
        val from = positionOf(state, actor)

        // Улица: клетка над выходом, соединена только с комнатой выхода.
        if (from == state.street || to == state.street) {
            return moveThroughExit(state, actor, from, to, extra)
        }

        val door = DungeonGeometry.doorId(from, to)
        // Завалённые клетки непроходимы для всех персонажей.
        return if (door != null && door in state.openDoors && to !in state.level.blocked) {
            val events = mutableListOf<DungeonEvent>(
                DungeonEvent.Move(
                    who = moverOf(actor),
                    hostageId = hostageIdOf(actor),
                    from = from,
                    to = to,
                ),
            )
            val next = resolveTurn(moveActor(state, actor, to, extra), actor, events, !extra)
            ActResult(ok = true, state = next, events = events)
        } else {
            reject(state)
        }
    }

    /** Переход улица ↔ выход — только герой; первый вход в партию бесплатный. */
    private fun moveThroughExit(
        state: DungeonState,
        actor: CharacterRef,
        from: Pos,
        to: Pos,
        extra: Boolean,
    ): ActResult {
        val crossing = (from == state.street && to == state.exitRoom) ||
            (from == state.exitRoom && to == state.street)
        // Герой выходит на улицу, только когда хотя бы один пленник уже вышел.
        val allowed = actor == CharacterRef.Hero && crossing &&
            (to == state.exitRoom || state.hostages.any { it.exited })
        if (!allowed) return reject(state)

        val events = mutableListOf<DungeonEvent>(
            DungeonEvent.Move(who = Mover.PLAYER, hostageId = null, from = from, to = to),
        )
        return if (from == state.street && state.turns == 0) {
            // Первый спуск с улицы — начало игры: не ход, враг не отвечает.
            ActResult(ok = true, state = state.copy(player = to), events = events)
        } else {
            val moved = state.copy(
                player = to,
                turns = state.turns + 1,
                extrasLeft = if (extra) state.extrasLeft - 1 else state.extrasLeft,
            )
            ActResult(ok = true, state = resolveTurn(moved, actor, events, enemyResponds = !extra), events = events)
        }
    }

    private fun moveActor(state: DungeonState, actor: CharacterRef, to: Pos, extra: Boolean): DungeonState {
        val extrasLeft = if (extra) state.extrasLeft - 1 else state.extrasLeft
        return when (actor) {
            CharacterRef.Hero -> state.copy(player = to, turns = state.turns + 1, extrasLeft = extrasLeft)
            is CharacterRef.Hostage -> state.copy(
                turns = state.turns + 1,
                extrasLeft = extrasLeft,
                hostages = state.hostages.map {
                    if (it.id == actor.id) it.copy(room = to) else it
                },
            )
        }
    }

    /** Освобождение, выход на улицу, смерти и (если [enemyResponds]) ход врага
     *  и победа — после хода за 1 очко. */
    private fun resolveTurn(
        state: DungeonState,
        actor: CharacterRef,
        events: MutableList<DungeonEvent>,
        enemyResponds: Boolean = true,
    ): DungeonState {
        val freed = freeHostages(state, at = positionOf(state, actor), events)
        val exited = exitHostages(freed, events)
        val dead = DungeonOutcome.checkDeaths(exited, events) ?: return if (enemyResponds) {
            DungeonOutcome.checkWin(DungeonEnemy.turn(exited, events), events)
        } else {
            DungeonOutcome.checkWin(exited, events)
        }
        return dead
    }

    /** Пленники в комнате [at] освобождаются — прикованный неуязвим, риск начинается здесь. */
    private fun freeHostages(
        state: DungeonState,
        at: Pos,
        events: MutableList<DungeonEvent>,
    ): DungeonState = state.copy(
        hostages = state.hostages.map { hostage ->
            if (!hostage.freed && !hostage.exited && hostage.room == at) {
                events += DungeonEvent.Free(hostage.id)
                hostage.copy(freed = true)
            } else {
                hostage
            }
        },
    )

    /** Освобождённый пленник, стоящий на выходе, автоматически выходит на улицу. */
    private fun exitHostages(
        state: DungeonState,
        events: MutableList<DungeonEvent>,
    ): DungeonState {
        val exiting = state.hostages.filter { it.freed && !it.exited && it.room == state.exitRoom }
        if (exiting.isEmpty()) return state
        exiting.forEach { events += DungeonEvent.Exit(it.id) }
        val exitedIds = exiting.map { it.id }.toSet()
        return state.copy(
            hostages = state.hostages.map {
                if (it.id in exitedIds) it.copy(exited = true) else it
            },
            active = when (val active = state.active) {
                is CharacterRef.Hostage if active.id in exitedIds -> CharacterRef.Hero
                else -> state.active
            },
        )
    }

    private fun toggle(state: DungeonState, door: DoorId): ActResult {
        // Двери открывает и закрывает только герой — и только внутри подземелья;
        // заклинившие двери на доске отсутствуют и переключению не подлежат;
        // при исчерпанном бюджете переключение отклоняется без траты хода.
        val allowed = state.active == CharacterRef.Hero &&
            DungeonGeometry.isInside(state.player, state.level) &&
            door in DungeonGeometry.adjacentDoors(state.player) &&
            door !in state.level.jammed &&
            state.togglesLeft > 0
        if (!allowed) return reject(state)

        val nowOpen = door !in state.openDoors
        val openDoors = if (nowOpen) state.openDoors + door else state.openDoors - door
        val events = mutableListOf<DungeonEvent>(DungeonEvent.Toggle(door = door, open = nowOpen))
        val togglesLeft = if (state.togglesLeft == Int.MAX_VALUE) Int.MAX_VALUE else state.togglesLeft - 1

        val next = DungeonEnemy.turn(
            state.copy(openDoors = openDoors, turns = state.turns + 1, togglesLeft = togglesLeft),
            events,
        )
        return ActResult(ok = true, state = DungeonOutcome.checkWin(next, events), events = events)
    }
}

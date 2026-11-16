package com.joy.sandbox.core.dungeon

import com.joy.sandbox.core.dungeon.model.CharacterRef
import com.joy.sandbox.core.dungeon.model.DoorId
import com.joy.sandbox.core.dungeon.model.DungeonLevel
import com.joy.sandbox.core.dungeon.model.DungeonLevels
import com.joy.sandbox.core.dungeon.model.DungeonState
import com.joy.sandbox.core.dungeon.model.GameOver
import com.joy.sandbox.core.dungeon.model.Pos
import com.joy.sandbox.core.dungeon.solutions.Solutions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Порт валидации из test/simulate.mjs: каждое эталонное решение
 * проигрывается через ядро, игра завершается победой ровно за пар ходов.
 * Первый бесплатный вход с улицы — действие без хода, поэтому число
 * действий может быть больше числа ходов.
 */
class LevelSolutionsTest {

    @Test
    fun level1SolvedInParMoves() = assertSolved(DungeonLevels.ALL[0], Solutions.LEVEL_1)

    @Test
    fun level2SolvedInParMoves() = assertSolved(DungeonLevels.ALL[1], Solutions.LEVEL_2)

    @Test
    fun level3SolvedInParMoves() = assertSolved(DungeonLevels.ALL[2], Solutions.LEVEL_3)

    @Test
    fun level4SolvedInParMoves() = assertSolved(DungeonLevels.ALL[3], Solutions.LEVEL_4)

    @Test
    fun level5SolvedInParMoves() = assertSolved(DungeonLevels.ALL[4], Solutions.LEVEL_5)

    private fun assertSolved(level: DungeonLevel, actions: List<DungeonAction>) {
        var state = DungeonEngine.create(level)
        actions.forEachIndexed { index, action ->
            val result = DungeonEngine.act(state, action)
            assertTrue("уровень ${level.id}, действие ${index + 1} отклонено: $action", result.ok)
            state = result.state
        }
        assertEquals(GameOver.Result.WIN, state.over?.result)
        assertEquals(level.par, state.turns)
        assertTrue(state.hostages.all { it.exited })
        assertEquals(state.street, state.player)
    }
}

/** Поведенческие тесты правил — улица и ручное управление пленниками. */
class RulesUnitTest {

    @Test
    fun `threat rooms are enemy neighbors through open doors`() {
        val state = enterDungeon(DungeonEngine.create(DungeonLevels.byId(1)))
        // Враг в (0,1), все двери открыты: reachable — (0,0) сверху, (1,1) справа, (0,2) снизу.
        assertEquals(setOf(Pos(0, 0), Pos(1, 1), Pos(0, 2)), DungeonEngine.threatRooms(state))
    }

    @Test
    fun `closed door blocks threat room`() {
        val level = DungeonLevels.byId(1)
        val entered = enterDungeon(DungeonEngine.create(level))
        val state = entered.copy(openDoors = entered.openDoors - DoorId.vertical(0, 0))
        // Враг в (0,1); закрыта дверь v:0,0 вверх — reachable остаются (1,1) и (0,2).
        assertEquals(setOf(Pos(1, 1), Pos(0, 2)), DungeonEngine.threatRooms(state))
    }

    @Test
    fun `game starts on street and first entry is free`() {
        val state = DungeonEngine.create(DungeonLevels.byId(1))
        assertEquals(Pos(3, -1), state.player)
        assertEquals(0, state.turns)

        val entered = DungeonEngine.act(state, DungeonAction.Move(Pos(3, 0)))
        assertTrue(entered.ok)
        assertEquals(0, entered.state.turns)
        assertEquals(Pos(3, 0), entered.state.player)
    }

    @Test
    fun `hero cannot leave dungeon before a hostage exited`() {
        val entered = enterDungeon(DungeonEngine.create(DungeonLevels.byId(1)))
        val result = DungeonEngine.act(entered, DungeonAction.Move(Pos(3, -1)))
        assertFalse(result.ok)
        assertEquals(entered, result.state)
    }

    @Test
    fun `toggle far door is rejected and keeps state`() {
        val state = enterDungeon(DungeonEngine.create(DungeonLevels.byId(1)))
        val result = DungeonEngine.act(state, DungeonAction.Toggle(DoorId.horizontal(0, 3)))
        assertFalse(result.ok)
        assertEquals(state, result.state)
        assertTrue(result.events.isEmpty())
    }

    @Test
    fun `toggle adjacent door flips it and spends turn`() {
        val state = enterDungeon(DungeonEngine.create(DungeonLevels.byId(1)))
        val result = DungeonEngine.act(state, DungeonAction.Toggle(DoorId.vertical(3, 0)))
        assertTrue(result.ok)
        assertEquals(1, result.state.turns)
        assertFalse(DoorId.vertical(3, 0) in result.state.openDoors)
        assertEquals(DungeonEvent.Toggle(DoorId.vertical(3, 0), open = false), result.events.first())
    }

    @Test
    fun `doors are toggled by hero only`() {
        // 2×2: пленник освобождён, активен он — дверь переключать нельзя.
        val level = DungeonLevel(
            id = 96,
            par = 1,
            name = "test",
            w = 2,
            h = 2,
            player = Pos(1, 0),
            enemy = Pos(1, 1),
            hostages = listOf(Pos(0, 0)),
            closed = setOf(DoorId.vertical(1, 0)),
            intro = "",
        )
        var state = DungeonEngine.create(level)
        state = DungeonEngine.act(state, DungeonAction.Move(Pos(1, 0))).state // вход
        state = DungeonEngine.act(state, DungeonAction.Move(Pos(0, 0))).state // освобождение
        state = DungeonEngine.act(state, DungeonAction.Switch(CharacterRef.Hostage(0))).state
        val result = DungeonEngine.act(state, DungeonAction.Toggle(DoorId.horizontal(0, 0)))
        assertFalse(result.ok)
        assertEquals(state, result.state)
    }

    @Test
    fun `move through closed door is rejected`() {
        val entered = enterDungeon(DungeonEngine.create(DungeonLevels.byId(2)))
        val closed = DungeonEngine.act(entered, DungeonAction.Toggle(DoorId.vertical(3, 0))).state
        val result = DungeonEngine.act(closed, DungeonAction.Move(Pos(3, 1)))
        assertFalse(result.ok)
        assertEquals(closed, result.state)
    }

    @Test
    fun `move to non adjacent room is rejected`() {
        val state = enterDungeon(DungeonEngine.create(DungeonLevels.byId(1)))
        val result = DungeonEngine.act(state, DungeonAction.Move(Pos(1, 3)))
        assertFalse(result.ok)
        assertEquals(state, result.state)
    }

    @Test
    fun `trapped enemy stays in place`() {
        // 1×3: враг в (0,2) отрезан закрытой дверью v:0,1; герой запирает в:0,0 — враг в ловушке.
        val level = DungeonLevel(
            id = 99,
            par = 1,
            name = "test",
            w = 1,
            h = 3,
            player = Pos(0, 1),
            enemy = Pos(0, 2),
            hostages = listOf(Pos(0, 1)),
            closed = setOf(DoorId.vertical(0, 1)),
            intro = "",
        )
        var state = DungeonEngine.create(level)
        state = DungeonEngine.act(state, DungeonAction.Move(Pos(0, 0))).state // бесплатный вход
        state = DungeonEngine.act(state, DungeonAction.Toggle(DoorId.vertical(0, 0))).state
        assertTrue(state.enemyTrapped)
        assertEquals(Pos(0, 2), state.enemy)
    }

    @Test
    fun `freed hostage does not follow hero`() {
        // 2×2: пленник освобождён и остаётся в своей комнате — цепочки нет.
        val level = DungeonLevel(
            id = 95,
            par = 1,
            name = "test",
            w = 2,
            h = 2,
            player = Pos(1, 0),
            enemy = Pos(1, 1),
            hostages = listOf(Pos(0, 0)),
            closed = setOf(DoorId.vertical(1, 0)),
            intro = "",
        )
        var state = DungeonEngine.create(level)
        state = DungeonEngine.act(state, DungeonAction.Move(Pos(1, 0))).state // вход
        state = DungeonEngine.act(state, DungeonAction.Move(Pos(0, 0))).state // освобождение
        assertTrue(state.hostages.single().freed)
        assertEquals(Pos(0, 0), state.hostages.single().room)
    }

    @Test
    fun `switch is free and moves no one`() {
        val level = DungeonLevel(
            id = 94,
            par = 1,
            name = "test",
            w = 2,
            h = 2,
            player = Pos(1, 0),
            enemy = Pos(1, 1),
            hostages = listOf(Pos(0, 0)),
            closed = setOf(DoorId.vertical(1, 0)),
            intro = "",
        )
        var state = DungeonEngine.create(level)
        state = DungeonEngine.act(state, DungeonAction.Move(Pos(1, 0))).state
        state = DungeonEngine.act(state, DungeonAction.Move(Pos(0, 0))).state
        val turnsBefore = state.turns
        val enemyBefore = state.enemy

        val result = DungeonEngine.act(state, DungeonAction.Switch(CharacterRef.Hostage(0)))
        assertTrue(result.ok)
        assertEquals(turnsBefore, result.state.turns)
        assertEquals(enemyBefore, result.state.enemy)
        assertEquals(CharacterRef.Hostage(0), result.state.active)

        // Обратно на героя — тоже бесплатно.
        val back = DungeonEngine.act(result.state, DungeonAction.Switch(CharacterRef.Hero))
        assertTrue(back.ok)
        assertEquals(turnsBefore, back.state.turns)
    }

    @Test
    fun `switch to chained or exited hostage is rejected`() {
        val created = DungeonEngine.create(DungeonLevels.byId(1))
        val chained = DungeonEngine.act(created, DungeonAction.Switch(CharacterRef.Hostage(0)))
        assertFalse(chained.ok)

        val exited = created.copy(
            hostages = created.hostages.map { it.copy(freed = true, exited = true) },
        )
        val result = DungeonEngine.act(exited, DungeonAction.Switch(CharacterRef.Hostage(0)))
        assertFalse(result.ok)
    }

    @Test
    fun `chained hostage is invulnerable`() {
        // 3×2: пленник A прикован в (0,1); кратчайший путь врага к освобождённому B
        // лежит через комнату A — враг входит, но убить не может.
        val level = DungeonLevel(
            id = 93,
            par = 1,
            name = "test",
            w = 3,
            h = 2,
            player = Pos(2, 0),
            enemy = Pos(0, 0),
            hostages = listOf(Pos(0, 1), Pos(2, 1)),
            closed = setOf(DoorId.horizontal(0, 0)),
            intro = "",
        )
        var state = DungeonEngine.create(level)
        state = state.copy(
            hostages = state.hostages.mapIndexed { index, hostage ->
                if (index == 1) hostage.copy(freed = true) else hostage
            },
        )
        state = DungeonEngine.act(state, DungeonAction.Move(Pos(2, 0))).state // бесплатный вход
        state = DungeonEngine.act(state, DungeonAction.Toggle(DoorId.horizontal(1, 0))).state
        assertNull(state.over)
        assertEquals(Pos(0, 1), state.enemy)
        assertFalse(state.hostages[0].dead)
        assertFalse(state.hostages[1].dead)
    }

    @Test
    fun `freed hostage dies on enemy contact`() {
        // 2×2: враг ближе к освобождённому пленнику, чем к герою — идёт к нему и убивает.
        val level = DungeonLevel(
            id = 92,
            par = 1,
            name = "test",
            w = 2,
            h = 2,
            player = Pos(1, 0),
            enemy = Pos(0, 0),
            hostages = listOf(Pos(1, 0)),
            closed = emptySet(),
            intro = "",
        )
        var state = DungeonEngine.create(level)
        state = state.copy(
            hostages = state.hostages.map { it.copy(freed = true) },
            player = Pos(1, 1),
        )
        state = DungeonEngine.act(state, DungeonAction.Toggle(DoorId.vertical(1, 0))).state
        assertEquals(GameOver.Result.LOSE, state.over?.result)
        assertTrue(state.hostages.single().dead)
    }

    @Test
    fun `enemy tie-break prefers hero then lowest hostage id`() {
        // 3×3: герой и пленник равноудалены (по 2 шага), первые шаги различаются —
        // враг идёт к герою.
        val level = DungeonLevel(
            id = 91,
            par = 1,
            name = "test",
            w = 3,
            h = 3,
            player = Pos(1, 0),
            enemy = Pos(1, 1),
            hostages = listOf(Pos(0, 0), Pos(2, 2)),
            closed = emptySet(),
            intro = "",
        )
        var heroFirst = DungeonEngine.create(level)
        heroFirst = heroFirst.copy(
            hostages = heroFirst.hostages.map { it.copy(freed = true, room = Pos(2, 2)) },
            player = Pos(0, 2),
        )
        heroFirst = DungeonEngine.act(heroFirst, DungeonAction.Toggle(DoorId.horizontal(0, 2))).state
        assertNull(heroFirst.over)
        assertEquals(Pos(0, 1), heroFirst.enemy)

        // Герой на улице (вне досягаемости): равноудалённые пленники — выбирается меньший id.
        var lowestId = DungeonEngine.create(level)
        lowestId = lowestId.copy(
            hostages = lowestId.hostages.mapIndexed { index, hostage ->
                hostage.copy(freed = true, room = if (index == 0) Pos(0, 1) else Pos(2, 1))
            },
            turns = 1,
        )
        lowestId = DungeonEngine.act(lowestId, DungeonAction.Move(Pos(2, 0))).state
        assertEquals(Pos(0, 1), lowestId.enemy)
        assertEquals(GameOver.Result.LOSE, lowestId.over?.result)
        assertTrue(lowestId.hostages[0].dead)
        assertFalse(lowestId.hostages[1].dead)
    }

    @Test
    fun `full rescue ends with hero on street`() {
        // 2×2: враг заперт (все его двери заклинены); освобождаем пленника,
        // выводим его на улицу и выходим сами. Победа — только когда герой на улице.
        val level = DungeonLevel(
            id = 90,
            par = 4,
            name = "test",
            w = 2,
            h = 2,
            player = Pos(1, 0),
            enemy = Pos(0, 1),
            hostages = listOf(Pos(0, 0)),
            closed = setOf(
                DoorId.vertical(0, 0),
                DoorId.vertical(1, 0),
                DoorId.horizontal(0, 1),
            ),
            intro = "",
        )
        var state = DungeonEngine.create(level)
        state = DungeonEngine.act(state, DungeonAction.Move(Pos(1, 0))).state // вход, 0 ходов
        state = DungeonEngine.act(state, DungeonAction.Move(Pos(0, 0))).state // освобождение, 1 ход
        state = DungeonEngine.act(state, DungeonAction.Switch(CharacterRef.Hostage(0))).state
        state = DungeonEngine.act(state, DungeonAction.Move(Pos(1, 0))).state // пленник вышел, 2 хода
        assertTrue(state.hostages.single().exited)
        assertEquals(CharacterRef.Hero, state.active)

        state = DungeonEngine.act(state, DungeonAction.Move(Pos(1, 0))).state // герой к выходу, 3 хода
        assertNull(state.over) // герой ещё не на улице — победы нет
        state = DungeonEngine.act(state, DungeonAction.Move(Pos(1, -1))).state // на улицу, 4 хода
        assertEquals(GameOver.Result.WIN, state.over?.result)
        assertEquals(4, state.turns)
    }

    @Test
    fun `exit is resolved before enemy turn and death can follow`() {
        // 2×2: пленник выходит на улицу, но затем враг настигает героя — поражение.
        val level = DungeonLevel(
            id = 97,
            par = 1,
            name = "test",
            w = 2,
            h = 2,
            player = Pos(1, 1),
            enemy = Pos(0, 1),
            hostages = listOf(Pos(0, 0)),
            closed = emptySet(),
            intro = "",
        )
        var state = DungeonEngine.create(level)
        state = state.copy(
            hostages = state.hostages.map { it.copy(freed = true) },
            player = Pos(1, 1),
            active = CharacterRef.Hostage(0),
        )
        state = DungeonEngine.act(state, DungeonAction.Move(Pos(1, 0))).state
        assertTrue(state.hostages.single().exited)
        assertEquals(GameOver.Result.LOSE, state.over?.result)
        assertEquals(Pos(1, 1), state.enemy)
    }

    @Test
    fun `game over rejects further actions`() {
        val entered = enterDungeon(DungeonEngine.create(DungeonLevels.byId(1)))
        var state = DungeonEngine.act(entered, DungeonAction.Move(Pos(2, 0))).state
        state = state.copy(over = GameOver(GameOver.Result.LOSE, "test"))
        val result = DungeonEngine.act(state, DungeonAction.Move(Pos(1, 0)))
        assertFalse(result.ok)
        assertEquals(state, result.state)
    }

    @Test
    fun `door id round trips through parse`() {
        assertNull(DoorId.parse("x:1,2"))
        assertNull(DoorId.parse("h:1"))
        assertEquals(DoorId.horizontal(12, 3), DoorId.parse("h:12,3"))
        assertEquals(DoorId.vertical(0, 5), DoorId.parse("v:0,5"))
        assertEquals("v:0,5", DoorId.vertical(0, 5).toString())
    }

    /** Первый бесплатный спуск с улицы: переход героя в комнату выхода. */
    private fun enterDungeon(state: DungeonState) =
        DungeonEngine.act(state, DungeonAction.Move(state.exitRoom)).state
}

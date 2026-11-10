package com.joy.sandbox.core.dungeon

import com.joy.sandbox.core.dungeon.model.DoorId
import com.joy.sandbox.core.dungeon.model.DungeonLevel
import com.joy.sandbox.core.dungeon.model.DungeonLevels
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
            assertTrue("уровень ${level.id}, ход ${index + 1} отклонён: $action", result.ok)
            state = result.state
        }
        assertEquals(GameOver.Result.WIN, state.over?.result)
        assertEquals(level.par, state.turns)
        assertEquals(actions.size, state.turns)
        assertTrue(state.hostages.all { it.exited })
    }
}

/** Поведенческие тесты правил — руками сведённые сценарии из js/rules.js. */
class RulesUnitTest {

    @Test
    fun `threat rooms are enemy neighbors through open doors`() {
        val state = DungeonEngine.create(DungeonLevels.byId(1))
        // Враг в (0,1), все двери открыты: reachable — (0,0) сверху, (1,1) справа, (0,2) снизу.
        assertEquals(setOf(Pos(0, 0), Pos(1, 1), Pos(0, 2)), DungeonEngine.threatRooms(state))
    }

    @Test
    fun `closed door blocks threat room`() {
        val level = DungeonLevels.byId(1)
        val state = DungeonEngine.create(level).copy(
            openDoors = DungeonEngine.create(level).openDoors - DoorId.vertical(0, 0),
        )
        // Враг в (0,1); закрыта дверь v:0,0 вверх — reachable остаются (1,1) и (0,2).
        assertEquals(setOf(Pos(1, 1), Pos(0, 2)), DungeonEngine.threatRooms(state))
    }

    @Test
    fun `toggle far door is rejected and keeps state`() {
        val state = DungeonEngine.create(DungeonLevels.byId(1))
        val result = DungeonEngine.act(state, DungeonAction.Toggle(DoorId.horizontal(0, 3)))
        assertFalse(result.ok)
        assertEquals(state, result.state)
        assertTrue(result.events.isEmpty())
    }

    @Test
    fun `toggle adjacent door flips it and spends turn`() {
        val state = DungeonEngine.create(DungeonLevels.byId(1))
        val result = DungeonEngine.act(state, DungeonAction.Toggle(DoorId.vertical(3, 0)))
        assertTrue(result.ok)
        assertEquals(1, result.state.turns)
        assertFalse(DoorId.vertical(3, 0) in result.state.openDoors)
        assertEquals(DungeonEvent.Toggle(DoorId.vertical(3, 0), open = false), result.events.first())
    }

    @Test
    fun `move through closed door is rejected`() {
        val level = DungeonLevels.byId(2)
        val state = DungeonEngine.create(level)
        // В уровне 2 дверь v:3,0 открыта; закроем её и попробуем пройти вниз.
        val closed = DungeonEngine.act(state, DungeonAction.Toggle(DoorId.vertical(3, 0))).state
        val result = DungeonEngine.act(closed, DungeonAction.Move(Pos(3, 1)))
        assertFalse(result.ok)
        assertEquals(closed, result.state)
    }

    @Test
    fun `move to non adjacent room is rejected`() {
        val state = DungeonEngine.create(DungeonLevels.byId(1))
        val result = DungeonEngine.act(state, DungeonAction.Move(Pos(1, 3)))
        assertFalse(result.ok)
        assertEquals(state, result.state)
    }

    @Test
    fun `trapped enemy stays in place`() {
        // Коридор 1×3: выход (0,0), игрок (0,1), враг (0,2).
        val level = DungeonLevel(
            id = 99,
            par = 1,
            name = "test",
            w = 1,
            h = 3,
            player = Pos(0, 1),
            enemy = Pos(0, 2),
            hostages = listOf(Pos(0, 0)),
            closed = emptySet(),
            intro = "",
        )
        var state = DungeonEngine.create(level)
        state = DungeonEngine.act(state, DungeonAction.Toggle(DoorId.vertical(0, 1))).state
        assertTrue(state.enemyTrapped)
        assertEquals(Pos(0, 2), state.enemy)
    }

    @Test
    fun `freed hostage follows player and exits`() {
        // 2×2: выход (1,0); игрок стартует в выходе, враг не мешает.
        val level = DungeonLevel(
            id = 98,
            par = 1,
            name = "test",
            w = 2,
            h = 2,
            player = Pos(1, 0),
            enemy = Pos(1, 1),
            hostages = listOf(Pos(0, 0)),
            closed = emptySet(),
            intro = "",
        )
        val state = DungeonEngine.act(DungeonEngine.create(level), DungeonAction.Move(Pos(0, 0))).state
        assertEquals(GameOver.Result.WIN, state.over?.result)
        assertTrue(state.hostages.single().exited)
    }

    @Test
    fun `death is checked before win`() {
        // 2×2: пленник выходит в тот же ход, в котором Повелитель настигает игрока — проигрыш.
        val level = DungeonLevel(
            id = 97,
            par = 1,
            name = "test",
            w = 2,
            h = 2,
            player = Pos(1, 0),
            enemy = Pos(0, 1),
            hostages = listOf(Pos(0, 0)),
            closed = emptySet(),
            intro = "",
        )
        val state = DungeonEngine.act(DungeonEngine.create(level), DungeonAction.Move(Pos(0, 0))).state
        assertEquals(GameOver.Result.LOSE, state.over?.result)
        assertTrue(state.hostages.single().exited)
    }

    @Test
    fun `game over rejects further actions`() {
        val level = DungeonLevels.byId(1)
        var state = DungeonEngine.create(level)
        state = DungeonEngine.act(state, DungeonAction.Move(Pos(2, 0))).state
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
}

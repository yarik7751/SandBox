package com.joy.sandbox.core.dungeon

import com.joy.sandbox.core.dungeon.model.DungeonLevels
import com.joy.sandbox.core.dungeon.model.GameOver
import com.joy.sandbox.core.dungeon.model.Pos
import com.joy.sandbox.core.dungeon.solver.DungeonSolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Поведенческие тесты дополнительного хода: перемещение без ответа врага. */
class ExtraMoveRulesTest {

    @Test
    fun `extra move moves hero without enemy response`() {
        val level = DungeonLevels.byId(1).copy(extraMoves = 1)
        val entered = DungeonEngine.act(DungeonEngine.create(level), DungeonAction.Move(Pos(3, 0))).state
        // Враг в (0,1) ждёт; обычный ход (3,0)→(2,0) — враг ответит шагом.
        val result = DungeonEngine.act(entered, DungeonAction.ExtraMove(Pos(2, 0)))
        assertTrue(result.ok)
        assertEquals(Pos(2, 0), result.state.player)
        assertEquals(Pos(0, 1), result.state.enemy) // враг не ответил
        assertEquals(entered.turns + 1, result.state.turns)
        assertEquals(0, result.state.extrasLeft)
    }

    @Test
    fun `extra move is rejected when stock is empty`() {
        val level = DungeonLevels.byId(1).copy(extraMoves = 1)
        val entered = DungeonEngine.act(DungeonEngine.create(level), DungeonAction.Move(Pos(3, 0))).state
        val spent = DungeonEngine.act(entered, DungeonAction.ExtraMove(Pos(2, 0))).state

        val rejected = DungeonEngine.act(spent, DungeonAction.ExtraMove(Pos(1, 0)))
        assertFalse(rejected.ok)
        assertEquals(spent.turns, rejected.state.turns)
        assertEquals(spent.player, rejected.state.player)
    }

    @Test
    fun `extra move frees hostage without enemy response`() {
        // Мини-уровень: герой через одну тихую прогулку освобождает пленника,
        // враг далеко и не двигается.
        val level = com.joy.sandbox.core.dungeon.model.DungeonLevel(
            id = 120,
            par = 4,
            name = "test",
            w = 2,
            h = 2,
            player = Pos(1, 0),
            enemy = Pos(0, 1),
            hostages = listOf(Pos(0, 0)),
            closed = emptySet(),
            intro = "",
            extraMoves = 1,
        )
        val entered = DungeonEngine.act(DungeonEngine.create(level), DungeonAction.Move(Pos(1, 0))).state
        val result = DungeonEngine.act(entered, DungeonAction.ExtraMove(Pos(0, 0)))
        assertTrue(result.ok)
        assertTrue(result.state.hostages.single().freed)
        assertEquals(entered.turns + 1, result.state.turns)
        assertEquals(0, result.state.extrasLeft)
    }
}

/** Solver решает уровни с запасом дополнительных ходов. */
class ExtraMoveSolverTest {

    @Test
    fun `solver solves level with extra moves`() {
        val level = DungeonLevels.byId(1).copy(id = 121, extraMoves = 1)
        val solution = DungeonSolver.solve(level)
        assertNotNull("уровень ${level.id} не решается", solution)
        var state = DungeonEngine.create(level)
        solution!!.actions.forEach { action ->
            val result = DungeonEngine.act(state, action)
            assertTrue("уровень ${level.id}: действие отклонено: $action", result.ok)
            state = result.state
        }
        assertEquals(GameOver.Result.WIN, state.over?.result)
    }
}

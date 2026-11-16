package com.joy.sandbox.core.dungeon

import com.joy.sandbox.core.dungeon.model.DoorId
import com.joy.sandbox.core.dungeon.model.DungeonLevels
import com.joy.sandbox.core.dungeon.model.GameOver
import com.joy.sandbox.core.dungeon.model.Pos
import com.joy.sandbox.core.dungeon.solver.DungeonSolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Поведенческие тесты дверной оси: лимит переключений и заряд врага. */
class DoorAxisRulesTest {

    @Test
    fun `toggle is rejected without spending turn when budget is exhausted`() {
        val level = DungeonLevels.byId(1).copy(togglesBudget = 1)
        val entered = DungeonEngine.act(DungeonEngine.create(level), DungeonAction.Move(Pos(3, 0))).state

        val first = DungeonEngine.act(entered, DungeonAction.Toggle(DoorId.horizontal(2, 0)))
        assertTrue(first.ok)
        assertEquals(0, first.state.togglesLeft)

        val second = DungeonEngine.act(first.state, DungeonAction.Toggle(DoorId.horizontal(2, 0)))
        assertFalse(second.ok)
        // Отклонение без траты хода: ходов не прибавилось, состояние как было.
        assertEquals(first.state.turns, second.state.turns)
        assertEquals(first.state.openDoors, second.state.openDoors)
    }

    @Test
    fun `trapped enemy with charge forces open first door giving path`() {
        val level = DungeonLevels.byId(1)
        val entered = DungeonEngine.act(DungeonEngine.create(level), DungeonAction.Move(Pos(3, 0))).state
        // Врага в (0,1) запечатываем, у него один заряд.
        val sealed = entered.copy(
            enemyChargesLeft = 1,
            openDoors = entered.openDoors - setOf(
                DoorId.vertical(0, 0),
                DoorId.horizontal(0, 1),
                DoorId.vertical(0, 1),
            ),
        )
        val result = DungeonEngine.act(sealed, DungeonAction.Move(Pos(2, 0)))
        assertTrue(result.ok)
        // Первый кандидат в порядке DIRS (вверх: v:0,0) даёт путь — выломан он.
        assertTrue(DoorId.vertical(0, 0) in result.state.openDoors)
        assertEquals(0, result.state.enemyChargesLeft)
        assertEquals(Pos(0, 1), result.state.enemy)
        assertFalse(result.state.enemyTrapped)
        assertTrue(result.events.any { it is DungeonEvent.EnemyDoorOpen && it.door == DoorId.vertical(0, 0) })
    }

    @Test
    fun `trapped enemy without charge stays trapped`() {
        val level = DungeonLevels.byId(1)
        val entered = DungeonEngine.act(DungeonEngine.create(level), DungeonAction.Move(Pos(3, 0))).state
        val sealed = entered.copy(
            openDoors = entered.openDoors - setOf(
                DoorId.vertical(0, 0),
                DoorId.horizontal(0, 1),
                DoorId.vertical(0, 1),
            ),
        )
        val result = DungeonEngine.act(sealed, DungeonAction.Move(Pos(2, 0)))
        assertTrue(result.ok)
        assertTrue(result.state.enemyTrapped)
        assertEquals(sealed.openDoors, result.state.openDoors)
        assertTrue(result.events.none { it is DungeonEvent.EnemyDoorOpen })
    }
}

/** Solver решает модельные уровни с механиками дверной оси. */
class DoorAxisSolverTest {

    @Test
    fun `solver solves level within toggle budget`() {
        val level = DungeonLevels.byId(1).copy(id = 111)
        val free = DungeonSolver.solve(level)
        assertNotNull(free)
        // Бюджет ровно под расход найденного решения — уровень решается.
        val used = free!!.actions.count { it is DungeonAction.Toggle }
        assertSolvable(level.copy(togglesBudget = used))
    }

    @Test
    fun `solver solves level with enemy door charge`() = assertSolvable(
        DungeonLevels.byId(2).copy(id = 112, enemyDoorCharge = 1),
    )

    private fun assertSolvable(level: com.joy.sandbox.core.dungeon.model.DungeonLevel) {
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

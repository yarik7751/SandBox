package com.joy.sandbox.core.dungeon

import com.joy.sandbox.core.dungeon.model.DoorId
import com.joy.sandbox.core.dungeon.model.DungeonLevels
import com.joy.sandbox.core.dungeon.model.GameOver
import com.joy.sandbox.core.dungeon.model.Pos
import com.joy.sandbox.core.dungeon.solver.DungeonSolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/** Поведенческие тесты механик уровней: завалы, укрытия, заклиневшие двери. */
class MechanicsRulesTest {

    @Test
    fun `blocked cell is excluded from threat rooms`() {
        val level = DungeonLevels.byId(1).copy(blocked = setOf(Pos(0, 0)))
        val state = DungeonEngine.create(level)
        // Враг в (0,1), все двери открыты; завал (0,0) исключён из угроз.
        assertEquals(setOf(Pos(1, 1), Pos(0, 2)), DungeonEngine.threatRooms(state))
    }

    @Test
    fun `blocked cell is not enterable`() {
        val level = DungeonLevels.byId(1).copy(blocked = setOf(Pos(2, 0)))
        val entered = DungeonEngine.act(DungeonEngine.create(level), DungeonAction.Move(Pos(3, 0))).state
        // Герой в (3,0), дверь h:2,0 открыта, но клетка (2,0) завалена.
        val move = DungeonEngine.act(entered, DungeonAction.Move(Pos(2, 0)))
        assertFalse(move.ok)
        assertEquals(entered.player, move.state.player)
    }

    @Test
    fun `create rejects blocked start cells`() {
        val level = DungeonLevels.byId(1).copy(blocked = setOf(Pos(0, 3)))
        assertThrows(IllegalArgumentException::class.java) {
            DungeonEngine.create(level)
        }
    }

    @Test
    fun `concealed cell is excluded from threat rooms`() {
        val level = DungeonLevels.byId(1).copy(concealed = setOf(Pos(1, 1)))
        val state = DungeonEngine.create(level)
        // Враг в (0,1); в укрытие (1,1) он не входит.
        assertEquals(setOf(Pos(0, 0), Pos(0, 2)), DungeonEngine.threatRooms(state))
    }

    @Test
    fun `enemy ignores hero on concealed cell`() {
        val level = DungeonLevels.byId(1).copy(concealed = setOf(Pos(1, 1)))
        val state = DungeonEngine.create(level).copy(player = Pos(1, 1))
        // Герой в укрытии рядом с врагом — целей нет, враг стоит и отмечен как в ловушке.
        val result = DungeonEngine.act(state, DungeonAction.Toggle(DoorId.horizontal(1, 1)))
        assertTrue(result.ok)
        assertEquals(Pos(0, 1), result.state.enemy)
        assertTrue(result.state.enemyTrapped)
    }

    @Test
    fun `jammed doors are absent from board and not toggleable`() {
        val door = DoorId.horizontal(2, 0)
        val level = DungeonLevels.byId(1).copy(jammed = setOf(door))
        val state = DungeonEngine.create(level)
        assertFalse(door in DungeonGeometry.allDoors(level))
        assertFalse(door in state.openDoors)

        val entered = DungeonEngine.act(state, DungeonAction.Move(Pos(3, 0))).state
        val toggled = DungeonEngine.act(entered, DungeonAction.Toggle(door))
        assertFalse(toggled.ok)
        assertEquals(entered.turns, toggled.state.turns)
    }
}

/** Solver решает модельные уровни с каждой из новых механик. */
class MechanicsSolverTest {

    @Test
    fun `solver solves level with blocked cells`() = assertSolvable(
        DungeonLevels.byId(1).copy(id = 101, blocked = setOf(Pos(0, 0))),
    )

    @Test
    fun `solver solves level with concealed cells`() = assertSolvable(
        DungeonLevels.byId(1).copy(id = 102, concealed = setOf(Pos(1, 1))),
    )

    @Test
    fun `solver solves level with jammed doors`() = assertSolvable(
        DungeonLevels.byId(1).copy(id = 103, jammed = setOf(DoorId.horizontal(0, 2))),
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

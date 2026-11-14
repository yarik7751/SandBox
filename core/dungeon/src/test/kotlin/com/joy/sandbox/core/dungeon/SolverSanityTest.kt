package com.joy.sandbox.core.dungeon

import com.joy.sandbox.core.dungeon.model.DoorId
import com.joy.sandbox.core.dungeon.model.DungeonLevel
import com.joy.sandbox.core.dungeon.model.Pos
import com.joy.sandbox.core.dungeon.solver.DungeonSolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/** Санити-проверка солвера на крошечных уровнях с известным ответом. */
class SolverSanityTest {

    @Test
    fun `sealed enemy mini level solved in 4 turns`() {
        // Тот же сценарий, что в `full rescue ends with hero on street`: пар = 4.
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
        val solution = DungeonSolver.solve(level)
        assertNotNull(solution)
        assertEquals(4, solution!!.turns)
        // Решение реально проигрывается ядром.
        var state = DungeonEngine.create(level)
        solution.actions.forEach { state = DungeonEngine.act(state, it).state }
        assertEquals(com.joy.sandbox.core.dungeon.model.GameOver.Result.WIN, state.over?.result)
    }
}

package com.joy.sandbox.core.dungeon

import com.joy.sandbox.core.dungeon.model.DungeonLevels
import com.joy.sandbox.core.dungeon.solver.DungeonSolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Перепроверка проходимости мультиагентным A* (DungeonSolver): каждый
 * уровень решается, и решение оптимально по ходам — совпадает с паром.
 */
class SolverTest {

    @Test
    fun allLevelsAreSolvableInPar() {
        DungeonLevels.ALL.forEach { level ->
            val solution = DungeonSolver.solve(level, weight = 1, maxExpansions = MAX_EXPANSIONS)
            assertNotNull("уровень ${level.id} не решён солвером", solution)
            assertEquals(
                "уровень ${level.id}: пар ${level.par} != оптимуму ${solution!!.turns}",
                level.par,
                solution.turns,
            )
        }
    }

    private companion object {

        const val MAX_EXPANSIONS = 3_000_000
    }
}

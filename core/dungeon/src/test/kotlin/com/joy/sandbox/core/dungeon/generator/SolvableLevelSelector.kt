package com.joy.sandbox.core.dungeon.generator

import com.joy.sandbox.core.dungeon.model.DungeonLevel
import com.joy.sandbox.core.dungeon.solver.DungeonSolver

/**
 * Отбор решаемых кандидатов solver'ом — общий для property-тестов генератора
 * и сборки JSON-пака (docs/dungeon-generation.md): уровень N — первый
 * решаемый attempt; пар берётся из решения.
 */
object SolvableLevelSelector {

    /**
     * Первая подходящая попытка уровня [n] с заполненным паром; null если
     * не нашлось. Отбор требует решаемости и нетривиальности: слишком
     * короткий пар означает случайно скомпонованный пустой уровень.
     */
    fun select(n: Int, maxAttempts: Int = MAX_ATTEMPTS): DungeonLevel? {
        for (attempt in 0 until maxAttempts) {
            val candidate = LevelGenerator.generate(n, attempt)
            val solution = DungeonSolver.solve(candidate, maxExpansions = MAX_EXPANSIONS)
            if (solution != null && solution.turns >= MIN_TURNS) {
                return candidate.copy(par = solution.turns)
            }
        }
        return null
    }

    private const val MAX_ATTEMPTS = 200
    private const val MAX_EXPANSIONS = 1_000_000
    private const val MIN_TURNS = 12
}

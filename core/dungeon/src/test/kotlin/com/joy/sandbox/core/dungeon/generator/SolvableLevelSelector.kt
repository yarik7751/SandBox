package com.joy.sandbox.core.dungeon.generator

import com.joy.sandbox.core.dungeon.DungeonAction
import com.joy.sandbox.core.dungeon.model.DungeonLevel
import com.joy.sandbox.core.dungeon.solver.DungeonSolver

/**
 * Отбор решаемых кандидатов solver'ом — общий для property-тестов генератора
 * и сборки JSON-пака (docs/dungeon-generation.md): уровень N — первый
 * решаемый attempt; пар берётся из решения. С [BUDGET_FROM] уровня кандидат
 * получает лимит переключений: сколько тратит найденное решение + запас, —
 * и обязан решаться повторно уже с бюджетом.
 */
object SolvableLevelSelector {

    /** Первая подходящая попытка уровня [n] с заполненным паром; null если
     *  не нашлось. Отбор требует решаемости и нетривиальности: слишком
     *  короткий пар означает случайно скомпонованный пустой уровень. */
    fun select(n: Int, maxAttempts: Int = MAX_ATTEMPTS): DungeonLevel? {
        for (attempt in 0 until maxAttempts) {
            val selected = trySelect(n, attempt)
            if (selected != null) return selected
        }
        return null
    }

    private fun trySelect(n: Int, attempt: Int): DungeonLevel? {
        val candidate = LevelGenerator.generate(n, attempt)
        val solution = DungeonSolver.solve(candidate, maxExpansions = MAX_EXPANSIONS)
        return when {
            solution == null || solution.turns < MIN_TURNS -> null
            n < BUDGET_FROM -> candidate.copy(par = solution.turns)
            else -> withBudget(candidate, solution)
        }
    }

    /** Уровень с лимитом переключений: расход решения + запас; с бюджетом обязан решаться повторно. */
    private fun withBudget(candidate: DungeonLevel, solution: DungeonSolver.Solution): DungeonLevel? {
        val togglesUsed = solution.actions.count { it is DungeonAction.Toggle }
        val budgeted = candidate.copy(togglesBudget = togglesUsed + BUDGET_MARGIN)
        val recheck = DungeonSolver.solve(budgeted, maxExpansions = MAX_EXPANSIONS)
        return recheck?.let { budgeted.copy(par = it.turns) }
    }

    private const val MAX_ATTEMPTS = 200
    // Узкий бюджет раскрытий: тяжёлые кандидаты (6×6) быстрее отбраковывать
    // и переходить к следующей попытке — итоговый уровень не меняется в среднем.
    private const val MAX_EXPANSIONS = 400_000
    private const val MIN_TURNS = 12
    private const val BUDGET_FROM = 35
    private const val BUDGET_MARGIN = 2
}

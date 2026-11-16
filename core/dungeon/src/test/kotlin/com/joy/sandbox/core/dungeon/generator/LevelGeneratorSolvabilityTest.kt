package com.joy.sandbox.core.dungeon.generator

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Решаемость сгенерированных уровней (docs/dungeon-generation.md): «N сидов
 * → каждый решается в пределах бюджета раскрытий, par в диапазоне». Тяжёлый
 * тест — отдельный класс, чтобы прогонять независимо от дешёвых property-тестов.
 * Уровни сверх диапазона валидируются сборкой пака (каждый отобранный
 * кандидат прогоняется solver'ом).
 */
class LevelGeneratorSolvabilityTest {

    @Test
    fun `first solvable attempt exists and par is sane`() {
        (GENERATED_FROM..SOLVABLE_RANGE).forEach { n ->
            val level = SolvableLevelSelector.select(n)
            assertNotNull("уровень $n: нет решаемой попытки", level)
            assertTrue(
                "уровень $n: пар ${level!!.par} вне диапазона $PAR_RANGE",
                level.par in PAR_RANGE,
            )
        }
    }

    private companion object {

        const val GENERATED_FROM = 6
        const val SOLVABLE_RANGE = 30
        val PAR_RANGE = 5..150
    }
}

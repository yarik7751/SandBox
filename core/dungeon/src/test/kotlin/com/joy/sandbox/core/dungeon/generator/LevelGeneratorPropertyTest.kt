package com.joy.sandbox.core.dungeon.generator

import com.joy.sandbox.core.dungeon.DungeonEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Property-тесты генератора (docs/dungeon-generation.md): «N сидов → каждый
 * решается в пределах бюджета раскрытий, par в диапазоне, выход не завален».
 */
class LevelGeneratorPropertyTest {

    @Test
    fun `generation is deterministic by seed`() {
        (1..RANGE).forEach { n ->
            assertEquals(LevelGenerator.generate(n), LevelGenerator.generate(n))
            assertEquals(LevelGenerator.generate(n, attempt = 3), LevelGenerator.generate(n, attempt = 3))
        }
    }

    @Test
    fun `neighboring levels differ`() {
        (1 until RANGE).forEach { n ->
            assertNotEquals(LevelGenerator.generate(n), LevelGenerator.generate(n + 1))
        }
    }

    @Test
    fun `layout passes engine validation`() {
        (1..RANGE).forEach { n ->
            DungeonEngine.create(LevelGenerator.generate(n))
        }
    }

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

        const val RANGE = 60
        const val GENERATED_FROM = 6
        const val SOLVABLE_RANGE = 35
        val PAR_RANGE = 5..150
    }
}

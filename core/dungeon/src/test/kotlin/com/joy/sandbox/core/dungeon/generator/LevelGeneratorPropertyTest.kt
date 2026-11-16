package com.joy.sandbox.core.dungeon.generator

import com.joy.sandbox.core.dungeon.DungeonEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Property-тесты генератора (docs/dungeon-generation.md): детерминизм,
 * различие соседних уровней, валидация раскладки ядром.
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

    private companion object {

        const val RANGE = 60
    }
}

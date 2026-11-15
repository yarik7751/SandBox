package com.joy.sandbox.core.dungeon.generator

import java.io.File
import org.junit.Assume.assumeTrue
import org.junit.Test

/**
 * Сборка JSON-пака уровней, вариант А из docs/dungeon-generation.md:
 * рукотворные уровни 1..5 + детерминированные сгенерированные 6..105.
 * Ручной запуск (чанками, т.к. solver гоняет каждый уровень):
 * ./gradlew :core:dungeon:test --tests '*DungeonPackGeneratorTest*' \
 *     -Dgenpack=true -Dpack.from=6 -Dpack.to=25
 * Каждый чанк пишет dungeon_levels_pack_<from>_<to>.json; итоговый пак —
 * объединение чанков. Рукотворные уровни 1..5 в пак не входят: приложение
 * само объединяет зашитые DungeonLevels.ALL с уровнями пака.
 */
class DungeonPackGeneratorTest {

    @Test
    fun generatePack() {
        assumeTrue("ручной запуск: -Dgenpack=true", System.getProperty("genpack") == "true")

        val from = System.getProperty("pack.from")?.toInt() ?: PACK_FROM
        val to = System.getProperty("pack.to")?.toInt() ?: PACK_TO
        val generated = (from..to).map { n ->
            val level = SolvableLevelSelector.select(n)
            requireNotNull(level) { "уровень $n: не найдено решаемой попытки" }
            require(level.par in PAR_RANGE) { "уровень $n: пар ${level.par} вне диапазона" }
            level
        }

        val target = File(ASSETS_DIR, "dungeon_levels_pack_${from}_$to.json")
        target.parentFile?.mkdirs()
        target.writeText(DungeonPackJson.write(generated))
    }

    private companion object {

        const val PACK_FROM = 6
        const val PACK_TO = 105
        const val ASSETS_DIR = "../../feature/dungeon/impl/src/main/assets"
        val PAR_RANGE = 5..150
    }
}

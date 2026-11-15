package com.joy.sandbox.core.dungeon.generator

import com.joy.sandbox.core.dungeon.model.DungeonLevel
import com.joy.sandbox.core.dungeon.model.Pos

/**
 * Запись JSON-пака уровней, версия формата 1 (docs/dungeon-generation.md).
 * Формат версионируемый; id уровней глобально уникальны и стабильны —
 * сейв (`bestTurns`) ключуется по id, добавление новых паков не меняет код.
 *
 * Читающая сторона (приложение): id — Int; name, intro — строки; par, w, h —
 * Int; player, enemy, hostages, blocked, concealed — позиции {x, y};
 * closed, jammed — двери в строковом виде DoorId ("h:1,2" / "v:1,2",
 * парсится DoorId.parse).
 */
object DungeonPackJson {

    fun write(levels: List<DungeonLevel>): String {
        val body = levels.joinToString(",\n") { level ->
            """
            |  {
            |    "id": ${level.id},
            |    "name": "${level.name.jsonEscape()}",
            |    "par": ${level.par},
            |    "w": ${level.w},
            |    "h": ${level.h},
            |    "player": ${level.player.json()},
            |    "enemy": ${level.enemy.json()},
            |    "hostages": [${level.hostages.joinToString(", ") { it.json() }}],
            |    "closed": [${level.closed.joinToString(", ") { "\"$it\"" }}],
            |    "blocked": [${level.blocked.joinToString(", ") { it.json() }}],
            |    "concealed": [${level.concealed.joinToString(", ") { it.json() }}],
            |    "jammed": [${level.jammed.joinToString(", ") { "\"$it\"" }}],
            |    "intro": "${level.intro.jsonEscape()}"
            |  }
            """.trimMargin()
        }
        return "{\n  \"version\": 1,\n  \"levels\": [\n$body\n  ]\n}\n"
    }

    private fun Pos.json(): String = "{\"x\": $x, \"y\": $y}"

    private fun String.jsonEscape(): String = buildString(length) {
        this@jsonEscape.forEach { char ->
            when (char) {
                '"' -> append("\\\"")
                '\\' -> append("\\\\")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> append(char)
            }
        }
    }
}

package com.joy.sandbox.core.dungeon.model

/**
 * Идентификатор двери между двумя соседними комнатами.
 * 'h' — горизонтальная (левая комната в паре), 'v' — вертикальная (верхняя).
 * Строковое представление — как в прототипе: "h:1,2" / "v:1,2".
 */
data class DoorId(val horizontal: Boolean, val x: Int, val y: Int) {

    override fun toString(): String = "${if (horizontal) "h" else "v"}:$x,$y"

    companion object {

        private val PATTERN = Regex("([hv]):(\\d+),(\\d+)")

        fun horizontal(x: Int, y: Int): DoorId = DoorId(horizontal = true, x = x, y = y)

        fun vertical(x: Int, y: Int): DoorId = DoorId(horizontal = false, x = x, y = y)

        fun parse(id: String): DoorId? {
            val match = PATTERN.matchEntire(id) ?: return null
            return DoorId(
                horizontal = match.groupValues[1] == "h",
                x = match.groupValues[2].toInt(),
                y = match.groupValues[3].toInt(),
            )
        }
    }
}

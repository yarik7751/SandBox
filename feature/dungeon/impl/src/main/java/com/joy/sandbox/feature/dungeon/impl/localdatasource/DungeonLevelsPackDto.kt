package com.joy.sandbox.feature.dungeon.impl.localdatasource

/** JSON-пак уровней (assets/dungeon_levels_pack.json, формат 1). */
data class DungeonLevelsPackDto(
    val version: Int,
    val levels: List<DungeonLevelEntryDto>,
)

/** Один уровень пака; позиции — пары координат, двери — строки DoorId. */
data class DungeonLevelEntryDto(
    val id: Int,
    val name: String,
    val par: Int,
    val w: Int,
    val h: Int,
    val player: Pair<Int, Int>,
    val enemy: Pair<Int, Int>,
    val hostages: List<Pair<Int, Int>>,
    val closed: List<String>,
    val blocked: List<Pair<Int, Int>>,
    val concealed: List<Pair<Int, Int>>,
    val jammed: List<String>,
    val intro: String,
)

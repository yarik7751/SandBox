package com.joy.sandbox.feature.dungeon.impl.presentation.model

/** Элемент списка уровней на титульном экране. */
data class LevelItemUiModel(
    val id: Int,
    val name: String,
    val par: Int,
    val locked: Boolean,
    val bestTurns: Int?,
)

/** Состояние титульного экрана. */
data class DungeonTitleUiModel(
    val levels: List<LevelItemUiModel>,
    val showLevels: Boolean,
)

/** Состояние экрана интро уровня. */
data class DungeonIntroUiModel(
    val levelId: Int,
    val levelName: String,
    val intro: String,
    val par: Int,
)

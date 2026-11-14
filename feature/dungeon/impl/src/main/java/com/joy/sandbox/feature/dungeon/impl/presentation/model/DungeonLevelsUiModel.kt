package com.joy.sandbox.feature.dungeon.impl.presentation.model

/** Элемент списка уровней на экране выбора уровня. */
data class LevelItemUiModel(
    val id: Int,
    val name: String,
    val par: Int,
    val locked: Boolean,
    val bestTurns: Int?,
)

/** Состояние экрана выбора уровня. */
data class DungeonLevelsUiModel(
    val levels: List<LevelItemUiModel>,
)

/** Состояние экрана интро уровня. */
data class DungeonIntroUiModel(
    val levelId: Int,
    val levelName: String,
    val intro: String,
    val par: Int,
)

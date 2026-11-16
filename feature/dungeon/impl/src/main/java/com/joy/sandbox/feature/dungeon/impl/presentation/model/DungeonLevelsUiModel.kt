package com.joy.sandbox.feature.dungeon.impl.presentation.model

/** Элемент списка уровней на экране выбора уровня. [current] — следующий
 *  непройденный уровень (куда ведёт «Играть» с титульного экрана). */
data class LevelItemUiModel(
    val id: Int,
    val name: String,
    val locked: Boolean,
    val current: Boolean,
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
)

package com.joy.sandbox.feature.dungeon.impl.presentation.model

/** Комната на доске (координаты сетки). */
data class PosUi(val x: Int, val y: Int)

/** Пленник на доске. */
data class HostageUiModel(
    val id: Int,
    val room: PosUi,
    val freed: Boolean,
    val exited: Boolean,
)

/** Исход партии для диалогов. */
data class OverUiModel(
    val win: Boolean,
    val reason: String,
    val newRecord: Boolean,
)

/** Состояние игрового экрана. */
data class DungeonGameUiModel(
    val levelId: Int,
    val levelName: String,
    val width: Int,
    val height: Int,
    val turns: Int,
    val par: Int,
    val player: PosUi,
    val enemy: PosUi,
    val hostages: List<HostageUiModel>,
    /** Открытые двери в строковом формате "h:1,2" / "v:1,2". */
    val openDoors: Set<String>,
    val threatRooms: Set<PosUi>,
    val exitRoom: PosUi,
    val enemyTrapped: Boolean,
    val over: OverUiModel?,
    val canUndo: Boolean,
    val soundEnabled: Boolean,
    val vibrationEnabled: Boolean,
    /** Инкрементируется с каждым применённым действием — драйвер анимаций. */
    val moveSeq: Long,
)

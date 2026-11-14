package com.joy.sandbox.feature.dungeon.impl.presentation.model

/** Комната на доске (координаты сетки; улица — y == -1). */
data class PosUi(val x: Int, val y: Int)

/** Пленник на доске. */
data class HostageUiModel(
    val id: Int,
    val room: PosUi,
    val freed: Boolean,
    val exited: Boolean,
)

/** Персонаж в панели переключения. */
data class CharacterUiModel(
    /** null — герой, иначе id пленника. */
    val id: Int?,
    /** Доступен для переключения: скованный пленник — недоступен. */
    val selectable: Boolean,
    /** Активен сейчас (ходит). */
    val active: Boolean,
    /** На улице: герой стоит там или пленник уже вышел. */
    val onStreet: Boolean,
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
    /** Позиция героя; когда он на улице — комната выхода. */
    val player: PosUi,
    val enemy: PosUi,
    val hostages: List<HostageUiModel>,
    /** Открытые двери в строковом формате "h:1,2" / "v:1,2". */
    val openDoors: Set<String>,
    val threatRooms: Set<PosUi>,
    val exitRoom: PosUi,
    val enemyTrapped: Boolean,
    /** Герой на улице (клетка над выходом). */
    val heroOnStreet: Boolean,
    /** Герой — активный персонаж (иначе активен пленник). */
    val activeIsHero: Boolean,
    /** Панель переключения: герой и пленники. */
    val characters: List<CharacterUiModel>,
    val over: OverUiModel?,
    val canUndo: Boolean,
    val soundEnabled: Boolean,
    val vibrationEnabled: Boolean,
    /** Инкрементируется с каждым применённым действием — драйвер анимаций. */
    val moveSeq: Long,
)

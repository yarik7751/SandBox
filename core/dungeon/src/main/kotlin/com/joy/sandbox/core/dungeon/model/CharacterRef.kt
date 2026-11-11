package com.joy.sandbox.core.dungeon.model

/**
 * Управляемый персонаж: герой или освобождённый пленник.
 * Переключение между ними — бесплатное действие (не ход).
 */
sealed interface CharacterRef {

    data object Hero : CharacterRef

    data class Hostage(val id: Int) : CharacterRef
}

package com.joy.sandbox.feature.dungeon.impl.domain

/** Короткая вибрация при гибели (опционально, настраивается). */
interface DungeonVibrationController {

    fun setEnabled(enabled: Boolean)

    fun pulseOnDeath()

    /** Тап по заклиневшей двери: короткий импульс. */
    fun pulseOnJammed()
}

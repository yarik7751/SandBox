package com.joy.sandbox.feature.dungeon.impl.domain

/** Короткая вибрация при гибели (опционально, настраивается). */
interface DungeonVibrationController {

    fun setEnabled(enabled: Boolean)

    fun pulseOnDeath()
}

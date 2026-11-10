package com.joy.sandbox.feature.dungeon.impl.domain

import com.joy.sandbox.core.dungeon.DungeonEvent

/** Звуковой синтез событий игры (ToneGenerator, без аудиофайлов). */
interface DungeonSoundPlayer {

    fun setEnabled(enabled: Boolean)

    fun play(events: List<DungeonEvent>)

    /** Короткий сигнал отклонённого действия. */
    fun playDeny()
}

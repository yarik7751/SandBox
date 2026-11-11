package com.joy.sandbox.feature.dungeon.impl.data

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Handler
import android.os.Looper
import com.joy.sandbox.core.dungeon.DungeonEvent
import com.joy.sandbox.core.dungeon.Mover
import com.joy.sandbox.feature.dungeon.impl.domain.DungeonSoundPlayer
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Синтез звука через [ToneGenerator] — аналог WebAudio из js/game.js,
 * без аудиофайлов. Тоны приближены к таблице sfx прототипа.
 */
@Singleton
class DungeonSoundPlayerImpl @Inject constructor() : DungeonSoundPlayer {

    private val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, VOLUME)
    private val handler = Handler(Looper.getMainLooper())
    private var enabled: Boolean = true

    override fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }

    override fun play(events: List<DungeonEvent>) {
        events.forEach(::playEvent)
    }

    override fun playDeny() {
        playTone(ToneGenerator.TONE_PROP_NACK, DENY_DURATION_MS)
    }

    private fun playEvent(event: DungeonEvent) {
        when (event) {
            is DungeonEvent.Toggle -> if (event.open) {
                playTone(ToneGenerator.TONE_PROP_BEEP, OPEN_DURATION_MS)
            } else {
                playTone(ToneGenerator.TONE_PROP_NACK, CLOSE_DURATION_MS)
            }
            is DungeonEvent.Move -> if (event.who == Mover.ENEMY) {
                playTone(ToneGenerator.TONE_SUP_ERROR, ENEMY_DURATION_MS)
            } else {
                playTone(ToneGenerator.TONE_PROP_BEEP, STEP_DURATION_MS)
            }
            is DungeonEvent.Free -> playTone(ToneGenerator.TONE_PROP_ACK, FREE_DURATION_MS)
            is DungeonEvent.Exit -> playTone(ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE, EXIT_DURATION_MS)
            is DungeonEvent.Death -> playTone(ToneGenerator.TONE_SUP_CONGESTION, LOSE_DURATION_MS)
            is DungeonEvent.Switch -> Unit // переключение бесшумное
            is DungeonEvent.Win -> playWinMelody()
        }
    }

    /** Аппроксимация восходящей гаммы sfx 'win' прототипа. */
    private fun playWinMelody() {
        playTone(ToneGenerator.TONE_PROP_ACK, MELODY_NOTE_MS)
        handler.postDelayed(
            { playTone(ToneGenerator.TONE_PROP_BEEP, MELODY_NOTE_MS) },
            MELODY_NOTE_MS.toLong(),
        )
        handler.postDelayed(
            { playTone(ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE, MELODY_NOTE_MS * 2) },
            MELODY_NOTE_MS.toLong() * 2,
        )
    }

    private fun playTone(toneType: Int, durationMs: Int) {
        if (!enabled) return
        toneGenerator.startTone(toneType, durationMs)
    }

    private companion object {

        const val VOLUME = 80
        const val STEP_DURATION_MS = 60
        const val OPEN_DURATION_MS = 120
        const val CLOSE_DURATION_MS = 150
        const val ENEMY_DURATION_MS = 180
        const val FREE_DURATION_MS = 120
        const val EXIT_DURATION_MS = 220
        const val LOSE_DURATION_MS = 500
        const val DENY_DURATION_MS = 80
        const val MELODY_NOTE_MS = 140
    }
}

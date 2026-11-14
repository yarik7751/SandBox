package com.joy.sandbox.feature.dungeon.impl.data

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import com.joy.sandbox.feature.dungeon.impl.domain.DungeonVibrationController
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DungeonVibrationControllerImpl @Inject constructor(
    context: Context,
) : DungeonVibrationController {

    private val vibrator = context.getSystemService(Vibrator::class.java)
    private var enabled: Boolean = true

    override fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }

    override fun pulseOnDeath() {
        if (!enabled) return
        vibrator?.vibrate(
            VibrationEffect.createOneShot(
                DEATH_PULSE_MS,
                VibrationEffect.DEFAULT_AMPLITUDE,
            )
        )
    }

    private companion object {

        const val DEATH_PULSE_MS = 200L
    }
}

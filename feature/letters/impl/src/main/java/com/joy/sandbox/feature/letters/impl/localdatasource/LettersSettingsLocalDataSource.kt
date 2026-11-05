package com.joy.sandbox.feature.letters.impl.localdatasource

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Хранение выбора настроек показа букв (алфавит, порядок, звук)
 * в SharedPreferences. Значения — имена enum'ов (Alphabet.name,
 * LettersOrder.name) и boolean для звука.
 */
@Singleton
class LettersSettingsLocalDataSource @Inject constructor(
    context: Context,
) {

    private val preferences: SharedPreferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun loadAlphabet(): String? = preferences.getString(KEY_ALPHABET, null)

    fun loadOrder(): String? = preferences.getString(KEY_ORDER, null)

    fun loadSoundEnabled(): Boolean = preferences.getBoolean(KEY_SOUND_ENABLED, true)

    /** Длительность ночного режима в минутах; -1 — бесконечность. */
    fun loadNightMinutes(): Int = preferences.getInt(KEY_NIGHT_MINUTES, DEFAULT_NIGHT_MINUTES)

    fun saveAlphabet(alphabetName: String) {
        preferences.edit().putString(KEY_ALPHABET, alphabetName).apply()
    }

    fun saveOrder(orderName: String) {
        preferences.edit().putString(KEY_ORDER, orderName).apply()
    }

    fun saveSoundEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()
    }

    fun saveNightMinutes(minutes: Int) {
        preferences.edit().putInt(KEY_NIGHT_MINUTES, minutes).apply()
    }

    private companion object {

        const val PREFERENCES_NAME = "letters_settings"
        const val KEY_ALPHABET = "alphabet"
        const val KEY_ORDER = "order"
        const val KEY_SOUND_ENABLED = "sound_enabled"
        const val KEY_NIGHT_MINUTES = "night_minutes"
        const val DEFAULT_NIGHT_MINUTES = 20
    }
}

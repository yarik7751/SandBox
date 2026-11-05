package com.joy.sandbox.feature.letters.impl.localdatasource

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Хранение настроек ночного режима (алфавит, порядок, длительность
 * таймера) в SharedPreferences. Значения — имена enum'ов
 * (Alphabet.name, LettersOrder.name) и минуты таймера
 * (-1 — бесконечность).
 *
 * Источник данных ночного режима — отдельный от дневного
 * (см. [LettersDaySettingsLocalDataSource]): у каждого режима
 * свои алфавит и порядок показа.
 */
@Singleton
class LettersNightSettingsLocalDataSource @Inject constructor(
    context: Context,
) {

    private val preferences: SharedPreferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun loadAlphabet(): String? = preferences.getString(KEY_ALPHABET, null)

    fun loadOrder(): String? = preferences.getString(KEY_ORDER, null)

    fun loadTimerMinutes(): Int = preferences.getInt(KEY_TIMER_MINUTES, DEFAULT_TIMER_MINUTES)

    fun saveAlphabet(alphabetName: String) {
        preferences.edit().putString(KEY_ALPHABET, alphabetName).apply()
    }

    fun saveOrder(orderName: String) {
        preferences.edit().putString(KEY_ORDER, orderName).apply()
    }

    fun saveTimerMinutes(minutes: Int) {
        preferences.edit().putInt(KEY_TIMER_MINUTES, minutes).apply()
    }

    private companion object {

        const val PREFERENCES_NAME = "letters_night_settings"
        const val KEY_ALPHABET = "alphabet"
        const val KEY_ORDER = "order"
        const val KEY_TIMER_MINUTES = "timer_minutes"
        const val DEFAULT_TIMER_MINUTES = 20
    }
}

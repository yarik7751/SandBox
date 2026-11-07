package com.joy.sandbox.feature.letters.impl.localdatasource

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Хранение последнего выбранного режима настроек (имя LettersMode)
 * в SharedPreferences. Значение общее для дня и ночи — это состояние
 * экрана настроек, а не конкретного режима. null — режим ещё не
 * выбирался, выбор по умолчанию делает repository.
 */
@Singleton
class LettersModeLocalDataSource @Inject constructor(
    context: Context,
) {

    private val preferences: SharedPreferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun loadMode(): String? = preferences.getString(KEY_LAST_MODE, null)

    fun saveMode(modeName: String) {
        preferences.edit().putString(KEY_LAST_MODE, modeName).apply()
    }

    private companion object {

        const val PREFERENCES_NAME = "letters_mode"
        const val KEY_LAST_MODE = "last_mode"
    }
}

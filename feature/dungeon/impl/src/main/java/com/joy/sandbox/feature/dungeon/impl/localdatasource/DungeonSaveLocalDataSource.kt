package com.joy.sandbox.feature.dungeon.impl.localdatasource

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

private val Context.dungeonDataStore by preferencesDataStore(name = PREFERENCES_NAME)

private const val PREFERENCES_NAME = "podzemelie-save-v2"

/**
 * Сохранения «Подземелья» в DataStore Preferences — аналог localStorage
 * с ключом SAVE_KEY из js/game.js: разблокированные уровни, рекорды ходов
 * по уровням, настройки звука и вибрации. Имя хранилища bumped v1 → v2:
 * старые рекорды и разблокировки несопоставимы с новой механикой (улица,
 * ручное управление пленниками), поэтому прогресс начинается с чистого листа.
 */
@Singleton
class DungeonSaveLocalDataSource @Inject constructor(
    private val context: Context,
) {

    private val dataStore get() = context.dungeonDataStore

    suspend fun load(): DungeonSaveDto {
        val preferences = dataStore.data.first()
        return DungeonSaveDto(
            unlockedLevel = preferences[keyUnlocked] ?: DEFAULT_UNLOCKED,
            bestTurns = BEST_LEVEL_IDS.associateWith { levelId ->
                preferences[intPreferencesKey(bestKey(levelId))] ?: NO_BEST
            }.filterValues { it != NO_BEST },
            soundEnabled = preferences[keySound] ?: DEFAULT_ENABLED,
            vibrationEnabled = preferences[keyVibration] ?: DEFAULT_ENABLED,
        )
    }

    suspend fun saveUnlocked(level: Int) {
        dataStore.edit { it[keyUnlocked] = level }
    }

    suspend fun saveBest(levelId: Int, turns: Int) {
        dataStore.edit { it[intPreferencesKey(bestKey(levelId))] = turns }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        dataStore.edit { it[keySound] = enabled }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        dataStore.edit { it[keyVibration] = enabled }
    }

    private fun bestKey(levelId: Int): String = "best_$levelId"

    private companion object {

        const val DEFAULT_UNLOCKED = 1
        const val DEFAULT_ENABLED = true
        const val NO_BEST = -1

        val BEST_LEVEL_IDS = 1..5

        val keyUnlocked = intPreferencesKey("unlocked")
        val keySound = booleanPreferencesKey("sound")
        val keyVibration = booleanPreferencesKey("vibration")
    }
}

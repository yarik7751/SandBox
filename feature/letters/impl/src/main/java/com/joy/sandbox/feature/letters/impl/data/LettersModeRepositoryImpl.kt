package com.joy.sandbox.feature.letters.impl.data

import com.joy.sandbox.feature.letters.impl.domain.LettersMode
import com.joy.sandbox.feature.letters.impl.domain.LettersModeRepository
import com.joy.sandbox.feature.letters.impl.localdatasource.LettersModeLocalDataSource
import javax.inject.Inject

/**
 * Реализация [LettersModeRepository]: имя enum'а в SharedPreferences,
 * обратно — через valueOf; сохранённого значения нет или оно неизвестно
 * (например, после удаления варианта) — [LettersMode.NIGHT].
 */
class LettersModeRepositoryImpl @Inject constructor(
    private val localDataSource: LettersModeLocalDataSource,
) : LettersModeRepository {

    override fun loadMode(): LettersMode =
        localDataSource.loadMode()
            ?.let { name -> LettersMode.entries.firstOrNull { it.name == name } }
            ?: LettersMode.NIGHT

    override fun saveMode(mode: LettersMode) {
        localDataSource.saveMode(mode.name)
    }
}

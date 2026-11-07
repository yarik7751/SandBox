package com.joy.sandbox.feature.letters.impl.data

import com.joy.sandbox.feature.letters.api.model.Alphabet
import com.joy.sandbox.feature.letters.api.model.LettersOrder
import com.joy.sandbox.feature.letters.impl.data.mapper.LettersSettingsDataMapper
import com.joy.sandbox.feature.letters.impl.domain.LettersDaySettingsRepository
import com.joy.sandbox.feature.letters.impl.localdatasource.LettersDaySettingsLocalDataSource
import javax.inject.Inject

class LettersDaySettingsRepositoryImpl @Inject constructor(
    private val localDataSource: LettersDaySettingsLocalDataSource,
    private val mapper: LettersSettingsDataMapper,
) : LettersDaySettingsRepository {

    override fun getSelectedAlphabet(): Alphabet =
        mapper.toAlphabet(localDataSource.loadAlphabet())

    override fun getSelectedOrder(): LettersOrder =
        mapper.toOrder(localDataSource.loadOrder())

    override fun saveAlphabet(alphabet: Alphabet) {
        localDataSource.saveAlphabet(alphabet.name)
    }

    override fun saveOrder(order: LettersOrder) {
        localDataSource.saveOrder(order.name)
    }

    override fun getSoundEnabled(): Boolean =
        localDataSource.loadSoundEnabled()

    override fun saveSoundEnabled(enabled: Boolean) {
        localDataSource.saveSoundEnabled(enabled)
    }
}

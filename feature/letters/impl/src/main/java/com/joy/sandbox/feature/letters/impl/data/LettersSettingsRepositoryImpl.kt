package com.joy.sandbox.feature.letters.impl.data

import com.joy.sandbox.feature.letters.api.model.Alphabet
import com.joy.sandbox.feature.letters.api.model.LettersOrder
import com.joy.sandbox.feature.letters.impl.data.mapper.LettersSettingsDataMapper
import com.joy.sandbox.feature.letters.impl.domain.LettersSettingsRepository
import com.joy.sandbox.feature.letters.impl.localdatasource.LettersSettingsLocalDataSource
import javax.inject.Inject

class LettersSettingsRepositoryImpl @Inject constructor(
    private val localDataSource: LettersSettingsLocalDataSource,
    private val mapper: LettersSettingsDataMapper,
) : LettersSettingsRepository {

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

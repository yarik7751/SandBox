package com.joy.sandbox.feature.letters.impl.data

import com.joy.sandbox.feature.letters.api.model.Alphabet
import com.joy.sandbox.feature.letters.api.model.LettersOrder
import com.joy.sandbox.feature.letters.impl.data.mapper.LettersSettingsDataMapper
import com.joy.sandbox.feature.letters.impl.domain.LettersNightSettingsRepository
import com.joy.sandbox.feature.letters.impl.localdatasource.LettersNightSettingsLocalDataSource
import javax.inject.Inject

class LettersNightSettingsRepositoryImpl @Inject constructor(
    private val localDataSource: LettersNightSettingsLocalDataSource,
    private val mapper: LettersSettingsDataMapper,
) : LettersNightSettingsRepository {

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

    override fun getNightDurationMin(): Int = localDataSource.loadTimerMinutes()

    override fun saveNightDurationMin(minutes: Int) {
        localDataSource.saveTimerMinutes(minutes)
    }
}

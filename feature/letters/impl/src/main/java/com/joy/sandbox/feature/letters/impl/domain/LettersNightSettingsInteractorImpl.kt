package com.joy.sandbox.feature.letters.impl.domain

import com.joy.sandbox.feature.letters.api.model.Alphabet
import com.joy.sandbox.feature.letters.api.model.LettersOrder
import javax.inject.Inject

class LettersNightSettingsInteractorImpl @Inject constructor(
    private val settingsRepository: LettersNightSettingsRepository,
) : LettersNightSettingsInteractor {

    override fun getSelectedAlphabet(): Alphabet =
        settingsRepository.getSelectedAlphabet()

    override fun getSelectedOrder(): LettersOrder =
        settingsRepository.getSelectedOrder()

    override fun saveAlphabet(alphabet: Alphabet) {
        settingsRepository.saveAlphabet(alphabet)
    }

    override fun saveOrder(order: LettersOrder) {
        settingsRepository.saveOrder(order)
    }

    override fun getNightDurationMin(): Int = settingsRepository.getNightDurationMin()

    override fun saveNightDurationMin(minutes: Int) {
        settingsRepository.saveNightDurationMin(minutes)
    }
}

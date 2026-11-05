package com.joy.sandbox.feature.letters.impl.domain

import com.joy.sandbox.feature.letters.api.model.Alphabet
import com.joy.sandbox.feature.letters.api.model.LettersOrder
import javax.inject.Inject

class LettersInteractorImpl @Inject constructor(
    private val settingsRepository: LettersSettingsRepository,
) : LettersInteractor {

    override fun getLetters(alphabet: Alphabet, order: LettersOrder): List<Char> {
        val letters = when (alphabet) {
            Alphabet.CYRILLIC -> CYRILLIC_LETTERS
            Alphabet.LATIN -> LATIN_LETTERS
        }
        return when (order) {
            LettersOrder.ALPHABETICAL -> letters
            LettersOrder.RANDOM -> letters.shuffled()
        }
    }

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

    override fun isSoundEnabled(): Boolean =
        settingsRepository.getSoundEnabled()

    override fun saveSoundEnabled(enabled: Boolean) {
        settingsRepository.saveSoundEnabled(enabled)
    }

    override fun getNightDurationMin(): Int = settingsRepository.getNightDurationMin()

    override fun saveNightDurationMin(minutes: Int) {
        settingsRepository.saveNightDurationMin(minutes)
    }

    private companion object {

        /** А–Я плюс Ё на своём алфавитном месте (после Е). */
        val CYRILLIC_LETTERS: List<Char> =
            ('А'..'Е').toList() + 'Ё' + ('Ж'..'Я').toList()

        val LATIN_LETTERS: List<Char> = ('A'..'Z').toList()
    }
}

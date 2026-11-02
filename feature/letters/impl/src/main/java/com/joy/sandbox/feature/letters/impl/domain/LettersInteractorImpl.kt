package com.joy.sandbox.feature.letters.impl.domain

import com.joy.sandbox.feature.letters.api.model.Alphabet
import com.joy.sandbox.feature.letters.api.model.LettersOrder
import javax.inject.Inject

class LettersInteractorImpl @Inject constructor() : LettersInteractor {

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

    private companion object {

        /** А–Я плюс Ё на своём алфавитном месте (после Е). */
        val CYRILLIC_LETTERS: List<Char> =
            ('А'..'Е').toList() + 'Ё' + ('Ж'..'Я').toList()

        val LATIN_LETTERS: List<Char> = ('A'..'Z').toList()
    }
}

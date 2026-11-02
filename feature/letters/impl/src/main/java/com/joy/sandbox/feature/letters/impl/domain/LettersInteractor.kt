package com.joy.sandbox.feature.letters.impl.domain

import com.joy.sandbox.feature.letters.api.model.Alphabet
import com.joy.sandbox.feature.letters.api.model.LettersOrder

/**
 * Источник букв для показа: набор знаков выбранного алфавита
 * в заданном порядке.
 */
interface LettersInteractor {

    fun getLetters(alphabet: Alphabet, order: LettersOrder): List<Char>
}

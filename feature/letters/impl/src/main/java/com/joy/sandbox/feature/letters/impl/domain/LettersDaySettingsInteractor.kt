package com.joy.sandbox.feature.letters.impl.domain

import com.joy.sandbox.feature.letters.api.model.Alphabet
import com.joy.sandbox.feature.letters.api.model.LettersOrder

/**
 * Источник букв для дневного показа: набор знаков выбранного алфавита
 * в заданном порядке, а также сохранённый выбор дневных настроек
 * (алфавит, порядок, звук).
 */
interface LettersDaySettingsInteractor {

    fun getLetters(alphabet: Alphabet, order: LettersOrder): List<Char>

    fun getSelectedAlphabet(): Alphabet

    fun getSelectedOrder(): LettersOrder

    fun saveAlphabet(alphabet: Alphabet)

    fun saveOrder(order: LettersOrder)

    fun isSoundEnabled(): Boolean

    fun saveSoundEnabled(enabled: Boolean)
}

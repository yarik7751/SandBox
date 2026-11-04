package com.joy.sandbox.feature.letters.impl.domain

import com.joy.sandbox.feature.letters.api.model.Alphabet
import com.joy.sandbox.feature.letters.api.model.LettersOrder

/**
 * Хранение выбора настроек показа букв (алфавит, порядок).
 * При отсутствии сохранённого значения возвращает значения
 * по умолчанию.
 */
interface LettersSettingsRepository {

    fun getSelectedAlphabet(): Alphabet

    fun getSelectedOrder(): LettersOrder

    fun saveAlphabet(alphabet: Alphabet)

    fun saveOrder(order: LettersOrder)
}

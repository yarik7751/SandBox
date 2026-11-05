package com.joy.sandbox.feature.letters.impl.domain

import com.joy.sandbox.feature.letters.api.model.Alphabet
import com.joy.sandbox.feature.letters.api.model.LettersOrder

/**
 * Хранение дневных настроек показа букв (алфавит, порядок, звук).
 * При отсутствии сохранённого значения возвращает значения
 * по умолчанию.
 */
interface LettersDaySettingsRepository {

    fun getSelectedAlphabet(): Alphabet

    fun getSelectedOrder(): LettersOrder

    fun saveAlphabet(alphabet: Alphabet)

    fun saveOrder(order: LettersOrder)

    fun getSoundEnabled(): Boolean

    fun saveSoundEnabled(enabled: Boolean)
}

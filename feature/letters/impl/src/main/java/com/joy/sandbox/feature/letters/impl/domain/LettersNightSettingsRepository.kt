package com.joy.sandbox.feature.letters.impl.domain

import com.joy.sandbox.feature.letters.api.model.Alphabet
import com.joy.sandbox.feature.letters.api.model.LettersOrder

/**
 * Хранение настроек ночного режима: алфавит, порядок показа и
 * длительность таймера в минутах (-1 — бесконечность).
 * При отсутствии сохранённого значения возвращает значения
 * по умолчанию.
 */
interface LettersNightSettingsRepository {

    fun getSelectedAlphabet(): Alphabet

    fun getSelectedOrder(): LettersOrder

    fun saveAlphabet(alphabet: Alphabet)

    fun saveOrder(order: LettersOrder)

    fun getNightDurationMin(): Int

    fun saveNightDurationMin(minutes: Int)
}

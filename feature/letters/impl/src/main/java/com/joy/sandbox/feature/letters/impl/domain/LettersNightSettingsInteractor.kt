package com.joy.sandbox.feature.letters.impl.domain

import com.joy.sandbox.feature.letters.api.model.Alphabet
import com.joy.sandbox.feature.letters.api.model.LettersOrder

/**
 * Настройки ночного режима: алфавит и порядок показа (свои,
 * отдельные от дневного режима) и длительность таймера в минутах
 * (-1 — бесконечность).
 */
interface LettersNightSettingsInteractor {

    fun getSelectedAlphabet(): Alphabet

    fun getSelectedOrder(): LettersOrder

    fun saveAlphabet(alphabet: Alphabet)

    fun saveOrder(order: LettersOrder)

    fun getNightDurationMin(): Int

    fun saveNightDurationMin(minutes: Int)
}

package com.joy.sandbox.feature.letters.impl.data.mapper

import com.joy.sandbox.feature.letters.api.model.Alphabet
import com.joy.sandbox.feature.letters.api.model.LettersOrder
import javax.inject.Inject

/**
 * Маппинг сохранённых строковых значений в доменные enum'ы
 * с fallback на значение по умолчанию.
 */
class LettersSettingsDataMapper @Inject constructor() {

    fun toAlphabet(alphabetName: String?): Alphabet =
        alphabetName?.let { name ->
            runCatching { Alphabet.valueOf(name) }.getOrNull()
        } ?: DEFAULT_ALPHABET

    fun toOrder(orderName: String?): LettersOrder =
        orderName?.let { name ->
            runCatching { LettersOrder.valueOf(name) }.getOrNull()
        } ?: DEFAULT_ORDER

    private companion object {

        val DEFAULT_ALPHABET = Alphabet.CYRILLIC
        val DEFAULT_ORDER = LettersOrder.ALPHABETICAL
    }
}

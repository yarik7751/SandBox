package com.joy.sandbox.feature.letters.impl.presentation.settings.mapper

import com.joy.sandbox.feature.letters.api.model.Alphabet
import com.joy.sandbox.feature.letters.api.model.LettersOrder
import com.joy.sandbox.feature.letters.impl.R
import com.joy.sandbox.feature.letters.impl.presentation.settings.model.SettingsOptionUiModel
import javax.inject.Inject

class SettingsUiMapper @Inject constructor() {

    fun mapAlphabetOptions(selected: Alphabet): List<SettingsOptionUiModel<Alphabet>> =
        Alphabet.entries.map { alphabet ->
            SettingsOptionUiModel(
                value = alphabet,
                labelRes = when (alphabet) {
                    Alphabet.CYRILLIC -> R.string.letters_alphabet_cyrillic
                    Alphabet.LATIN -> R.string.letters_alphabet_latin
                },
                selected = alphabet == selected,
            )
        }

    fun mapOrderOptions(selected: LettersOrder): List<SettingsOptionUiModel<LettersOrder>> =
        LettersOrder.entries.map { order ->
            SettingsOptionUiModel(
                value = order,
                labelRes = when (order) {
                    LettersOrder.ALPHABETICAL -> R.string.letters_order_alphabetical
                    LettersOrder.RANDOM -> R.string.letters_order_random
                },
                selected = order == selected,
            )
        }
}

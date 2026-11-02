package com.joy.sandbox.feature.letters.impl.presentation.letters.mapper

import com.joy.sandbox.feature.letters.impl.presentation.letters.model.LetterUiModel
import javax.inject.Inject

class LetterUiModelMapper @Inject constructor() {

    fun map(char: Char): LetterUiModel = LetterUiModel(char = char)
}

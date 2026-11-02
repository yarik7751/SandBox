package com.joy.sandbox.feature.letters.impl.presentation.letters.mapper

import androidx.compose.ui.graphics.Color
import com.joy.sandbox.feature.letters.impl.presentation.letters.model.LetterUiModel
import javax.inject.Inject

class LetterUiModelMapper @Inject constructor() {

    fun map(char: Char, fillColor: Color): LetterUiModel = LetterUiModel(
        char = char,
        fillColor = fillColor,
    )
}

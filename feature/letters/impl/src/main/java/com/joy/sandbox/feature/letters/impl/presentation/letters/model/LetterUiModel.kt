package com.joy.sandbox.feature.letters.impl.presentation.letters.model

import androidx.compose.ui.graphics.Color
import com.joy.sandbox.feature.letters.impl.presentation.letters.model.FillDirection

/**
 * Буква на экране показа.
 */
data class LetterUiModel(
    val char: Char,
    val fillColor: Color,
    val fillDirection: FillDirection,
)

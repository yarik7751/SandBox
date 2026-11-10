package com.joy.sandbox.feature.letters.impl.presentation.letters

import androidx.compose.ui.graphics.Color
import javax.inject.Inject

/**
 * Палитра закраски букв — яркие насыщенные оттенки,
 * чтобы читались на чёрном фоне.
 */
class LetterColorProvider @Inject constructor() {

    private val palette: List<Color> = listOf(
        Color(0xFFEF5350), // красный
        Color(0xFFFFA726), // оранжевый
        Color(0xFFFFEB3B), // жёлтый
        Color(0xFFC0CA33), // лайм
        Color(0xFF66BB6A), // зелёный
        Color(0xFF26A69A), // бирюзовый
        Color(0xFF26C6DA), // голубой
        Color(0xFF42A5F5), // синий
        Color(0xFFAB47BC), // фиолетовый
        Color(0xFFEC407A), // розовый
    )

    /** Палитра в случайном порядке — на каждый запуск показа своя. */
    fun shuffledPalette(): List<Color> = palette.shuffled()
}

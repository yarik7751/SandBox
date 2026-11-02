package com.joy.sandbox.feature.letters.impl.presentation.letters

import androidx.compose.ui.graphics.Color
import javax.inject.Inject

/**
 * Палитра закраски букв — базовые цвета в тёмных оттенках,
 * чтобы читались на белом фоне.
 */
class LetterColorProvider @Inject constructor() {

    private val palette: List<Color> = listOf(
        Color(0xFFE53935), // красный
        Color(0xFFFB8C00), // оранжевый
        Color(0xFFFDD835), // жёлтый
        Color(0xFF43A047), // зелёный
        Color(0xFF039BE5), // голубой
        Color(0xFF3949AB), // синий
        Color(0xFF8E24AA), // фиолетовый
        Color(0xFFD81B60), // розовый
    )

    fun random(): Color = palette.random()
}

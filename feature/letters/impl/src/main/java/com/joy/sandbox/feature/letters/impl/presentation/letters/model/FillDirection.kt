package com.joy.sandbox.feature.letters.impl.presentation.letters.model

import androidx.compose.ui.geometry.Offset
import kotlin.math.sqrt

/**
 * Направление роста заливки буквы: от угла-источника [from] к целевому
 * углу. [axis] — единичный вектор роста: линия заливки движется вдоль
 * него по мере увеличения прогресса.
 */
enum class FillDirection(
    val axis: Offset,
) {

    BOTTOM_TO_TOP(axis = Offset(0f, -1f)),
    TOP_TO_BOTTOM(axis = Offset(0f, 1f)),
    BOTTOM_LEFT_TO_TOP_RIGHT(axis = Offset(1f, -1f) / DIAGONAL_LENGTH),
    TOP_RIGHT_TO_BOTTOM_LEFT(axis = Offset(-1f, 1f) / DIAGONAL_LENGTH),
    TOP_LEFT_TO_BOTTOM_RIGHT(axis = Offset(1f, 1f) / DIAGONAL_LENGTH),
    BOTTOM_RIGHT_TO_TOP_LEFT(axis = Offset(-1f, -1f) / DIAGONAL_LENGTH),
}

private val DIAGONAL_LENGTH = sqrt(2f)

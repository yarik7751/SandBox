package com.joy.sandbox.feature.letters.impl.presentation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Заголовок экрана фичи «Буквы» вместо тулбара: по центру ширины,
 * под верхним краем экрана.
 */
@Composable
internal fun LettersScreenTitle(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = TITLE_TOP_PADDING),
        color = color,
        fontSize = TITLE_FONT_SIZE,
        textAlign = TextAlign.Center,
    )
}

private val TITLE_TOP_PADDING = 16.dp
private val TITLE_FONT_SIZE = 28.sp

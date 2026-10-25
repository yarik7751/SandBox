package com.joy.sandbox.core.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Единый Toolbar для всех экранов приложения.
 *
 * Высота 56dp, нижние углы скруглены на 16dp, фон белый,
 * заголовок и стрелка назад — черные.
 *
 * [title] — заголовок экрана.
 * [onBackClick] — обработчик нажатия на стрелку назад; если null,
 * стрелка скрывается (например, на корневом экране стека).
 */
@Composable
fun SandBoxToolbar(
    title: String,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(TOOLBAR_HEIGHT),
        shape = RoundedCornerShape(bottomStart = TOOLBAR_CORNER, bottomEnd = TOOLBAR_CORNER),
        color = Color.White,
        // Тень по скруглённой форме — делает скругление заметным на любом фоне.
        shadowElevation = TOOLBAR_SHADOW,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = Color.Black,
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(16.dp))
            }
            Text(
                modifier = Modifier.weight(1f),
                text = title,
                color = Color.Black,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private val TOOLBAR_HEIGHT = 56.dp
private val TOOLBAR_CORNER = 16.dp
private val TOOLBAR_SHADOW = 4.dp

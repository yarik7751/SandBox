package com.joy.sandbox.core.ui

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Поле поиска с кнопкой очистки.
 *
 * Trailing-иконка «×» видна только при непустом запросе и стирает всё поле.
 * При фокусе индикатор и курсор выделяются брендовым синим
 * [DesignSystem.Colors.testBrandColor].
 */
@Composable
fun SearchTextField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
) {
    val focusColor = DesignSystem.Colors.testBrandColor
    OutlinedTextField(
        modifier = modifier
            .padding(vertical = 4.dp)
            .height(48.dp),
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge,
        placeholder = {
            if (placeholder != null) {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = focusColor,
            focusedLabelColor = focusColor,
            cursorColor = focusColor,
        ),
    )
}

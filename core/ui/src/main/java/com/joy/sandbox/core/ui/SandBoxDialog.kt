package com.joy.sandbox.core.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

/**
 * Единый диалог приложения.
 *
 * [mainActionText] — подтверждающее действие (справа), [neutralActionText] —
 * нейтральное действие (слева от основного). Закрытие диалога (клик вне,
 * системная кнопка назад) приводит к [onDismiss].
 */
@Composable
fun SandBoxDialog(
    title: String,
    description: String,
    mainActionText: String,
    onMainAction: () -> Unit,
    neutralActionText: String,
    onNeutralAction: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(onClick = onMainAction) {
                Text(text = mainActionText)
            }
        },
        dismissButton = {
            TextButton(onClick = onNeutralAction) {
                Text(text = neutralActionText)
            }
        },
    )
}

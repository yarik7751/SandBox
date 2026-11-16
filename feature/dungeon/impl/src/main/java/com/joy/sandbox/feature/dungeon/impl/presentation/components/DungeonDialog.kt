package com.joy.sandbox.feature.dungeon.impl.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.joy.sandbox.feature.dungeon.impl.presentation.theme.DungeonColors

/**
 * Диалог «Подземелья» как на мини-сайте: полноэкранный скрим,
 * центрированная панель с рамкой, серифный золотой заголовок
 * и вертикальное меню кнопок.
 */
@Composable
internal fun DungeonDialog(
    title: String,
    onDismiss: (() -> Unit)?,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DungeonColors.OverlayScrim)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { onDismiss?.invoke() },
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            color = DungeonColors.Panel,
            contentColor = DungeonColors.Text,
            border = BorderStroke(1.dp, DungeonColors.PanelEdge),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .padding(20.dp)
                .widthIn(max = 480.dp)
                // не пропускать тапы по панели в скрим (иначе закроет диалог)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) {},
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = DungeonColors.GoldBright,
                    textAlign = TextAlign.Center,
                )
                content()
            }
        }
    }
}

/**
 * Кнопка меню диалога (.btn на сайте): основная — золотая,
 * остальные — тёмные с рамкой. Полная ширина, высота от 48 dp.
 */
@Composable
internal fun DungeonMenuButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean = false,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, if (primary) DungeonColors.Gold else DungeonColors.PanelEdge),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (primary) DungeonColors.Gold else DungeonColors.ButtonBg,
            contentColor = if (primary) DungeonColors.OnGold else DungeonColors.Text,
        ),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (primary) FontWeight.SemiBold else null,
        )
    }
}

/** Второстепенный текст панели (.par / .note на сайте) — приглушённый. */
@Composable
internal fun DungeonDialogNote(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = DungeonColors.TextDim,
        textAlign = TextAlign.Center,
        modifier = modifier,
    )
}

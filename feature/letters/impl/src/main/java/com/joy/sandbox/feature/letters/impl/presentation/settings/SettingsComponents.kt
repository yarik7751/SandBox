package com.joy.sandbox.feature.letters.impl.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.joy.sandbox.core.ui.DesignSystem
import com.joy.sandbox.feature.letters.impl.presentation.settings.model.SettingsOptionUiModel

/** Кнопка действия экрана: на всю ширину, в цветах палитры экрана. */
@Composable
fun SettingsButton(
    text: String,
    palette: SettingsPalette,
    onClick: () -> Unit,
) {
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = palette.accent,
            contentColor = palette.buttonContent,
        ),
        onClick = onClick,
    ) {
        Text(text = text)
    }
}

/** Группа опций с заголовком: плитки на выбор. */
@Composable
fun <T> SettingsGroup(
    title: String,
    options: List<SettingsOptionUiModel<T>>,
    onOptionClick: (T) -> Unit,
    palette: SettingsPalette,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = palette.text,
    )
    options.forEach { option ->
        OptionTile(
            text = stringResource(option.labelRes),
            selected = option.selected,
            palette = palette,
            onClick = { onOptionClick(option.value) },
        )
    }
}

/** Плитка опции: серый фон — не выбрана, белый — выбрана. */
@Composable
fun OptionTile(
    text: String,
    selected: Boolean,
    palette: SettingsPalette,
    onClick: () -> Unit,
) {
    val background = if (selected) {
        palette.optionTileSelected
    } else {
        palette.optionTileUnselected
    }
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .clip(OPTION_TILE_SHAPE)
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        color = if (selected) {
            DesignSystem.Colors.Letters.OptionTileSelectedText
        } else {
            palette.text
        },
    )
}

/** Строка-переключатель с заголовком. */
@Composable
fun SoundSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    palette: SettingsPalette,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = palette.text,
        )
        Spacer(modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = palette.accent,
                checkedTrackColor = palette.switchTrackChecked,
            ),
        )
    }
}

private val OPTION_TILE_SHAPE = RoundedCornerShape(16.dp)

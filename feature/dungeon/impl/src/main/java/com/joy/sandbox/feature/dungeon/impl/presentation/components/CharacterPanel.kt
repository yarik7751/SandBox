package com.joy.sandbox.feature.dungeon.impl.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.joy.sandbox.feature.dungeon.impl.R
import com.joy.sandbox.feature.dungeon.impl.presentation.model.CharacterUiModel
import com.joy.sandbox.feature.dungeon.impl.presentation.theme.DungeonColors

/**
 * Панель переключения персонажей под статистикой: слева индикатор улицы
 * (кружки — кто уже на улице: заполненный кружок снаружи, полый — внутри,
 * герой первым), затем кнопки персонажей. Активный — с золотой рамкой,
 * скованные пленники — не кликаются. Тап по кнопке делает персонажа
 * активным: следующий тап по клетке доски походит им.
 */
@Composable
internal fun CharacterPanel(
    characters: List<CharacterUiModel>,
    onCharacterClick: (Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StreetIndicator(characters)
        Spacer(modifier = Modifier.width(6.dp))
        characters.forEach { character ->
            CharacterButton(character, onCharacterClick)
        }
    }
}

/** Кружки-состояния: заполненный — персонаж на улице, полый — внутри. */
@Composable
private fun StreetIndicator(characters: List<CharacterUiModel>) {
    val indicatorDescription = stringResource(R.string.dungeon_street_indicator)
    Row(
        modifier = Modifier.semantics { contentDescription = indicatorDescription },
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        characters.forEach { character ->
            val color = characterColor(character.id)
            Box(
                modifier = Modifier
                    .size(STREET_DOT_SIZE)
                    .clip(CircleShape)
                    .background(if (character.onStreet) color else Color.Transparent)
                    .border(STREET_DOT_STROKE, color, CircleShape),
            )
        }
    }
}

@Composable
private fun CharacterButton(
    character: CharacterUiModel,
    onCharacterClick: (Int?) -> Unit,
) {
    val label = if (character.id == null) {
        stringResource(R.string.dungeon_hero_button)
    } else {
        stringResource(R.string.dungeon_hostage_button, character.id)
    }
    Button(
        onClick = { onCharacterClick(character.id) },
        enabled = character.selectable,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(
            1.dp,
            if (character.active) DungeonColors.Gold else DungeonColors.PanelEdge,
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (character.active) DungeonColors.ButtonHover else DungeonColors.ButtonBg,
            contentColor = if (character.active) DungeonColors.GoldBright else DungeonColors.Text,
            disabledContainerColor = DungeonColors.ButtonBg,
            disabledContentColor = DungeonColors.TextDim,
        ),
        contentPadding = PaddingValues(horizontal = BUTTON_PADDING_HORIZONTAL),
        modifier = Modifier.heightIn(min = BUTTON_HEIGHT),
    ) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
    }
}

/** Цвет персонажа как на доске: герой — плащ, пленники — лаванда/серость старейшины. */
private fun characterColor(id: Int?): Color = when {
    id == null -> DungeonColors.PlayerCloak
    id == HOSTAGE_ELDER_ID -> DungeonColors.HostageElder
    else -> DungeonColors.Hostage
}

private const val HOSTAGE_ELDER_ID = 0
private val STREET_DOT_SIZE = 12.dp
private val STREET_DOT_STROKE = 1.5.dp
private val BUTTON_HEIGHT = 32.dp
private val BUTTON_PADDING_HORIZONTAL = 10.dp

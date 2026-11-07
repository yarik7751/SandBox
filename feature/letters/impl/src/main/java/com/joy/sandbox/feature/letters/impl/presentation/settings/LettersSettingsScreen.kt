package com.joy.sandbox.feature.letters.impl.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.joy.sandbox.core.navigation.router.JoyRouter
import com.joy.sandbox.core.ui.DesignSystem
import com.joy.sandbox.core.ui.SandBoxToolbar
import com.joy.sandbox.core.ui.SystemBarColors
import com.joy.sandbox.core.ui.ViewModelFactory
import com.joy.sandbox.feature.letters.api.LettersJoyScreen
import com.joy.sandbox.feature.letters.api.NightLettersJoyScreen
import com.joy.sandbox.feature.letters.impl.R
import com.joy.sandbox.feature.letters.impl.presentation.settings.LettersSettingsViewModel.Commands
import com.joy.sandbox.feature.letters.impl.presentation.settings.model.SettingsOptionUiModel
import com.joy.sandbox.utils.observe

/**
 * Настройки фичи «Буквы»: вкладки-режимы «День» и «Ночь» переключают
 * разделы; в каждом — настройки своего режима из своего источника
 * данных. «Запустить» открывает показ через replace — настройки
 * выпадают из стека, возврат с экрана показа попадает сразу на Главный.
 */
@Composable
fun LettersSettingsScreen(
    viewModelFactory: ViewModelFactory,
    router: JoyRouter,
) {
    val viewModel: LettersSettingsViewModel = viewModel(factory = viewModelFactory)
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    // Доля позиции ползунка табов (0 = «День», 1 = «Ночь») — фон экрана
    // и тулбар плавно лерпятся по ней, следуя за движением указателя.
    val thumbFraction = remember { mutableFloatStateOf(0f) }
    val fraction = thumbFraction.floatValue
    val toolbarBackground = lerp(
        DesignSystem.Colors.Letters.ToolbarBackgroundDay,
        DesignSystem.Colors.Letters.ToolbarBackgroundNight,
        fraction,
    )

    Scaffold(
        topBar = {
            SandBoxToolbar(
                title = stringResource(R.string.letters_settings_title),
                onBackClick = { router.exit() },
                backgroundColor = toolbarBackground,
                contentColor = lerp(
                    DesignSystem.Colors.Letters.ToolbarContentDay,
                    DesignSystem.Colors.Letters.ToolbarContentNight,
                    fraction,
                ),
            )
        },
    ) { padding ->
        val background = lerp(
            DesignSystem.Colors.Letters.SettingsBackgroundDay,
            DesignSystem.Colors.Letters.SettingsBackgroundNight,
            fraction,
        )
        // Status bar — в цвет тулбара, navigation bar — в цвет фона.
        SystemBarColors(
            statusBarColor = toolbarBackground,
            navigationBarColor = background,
            lightIcons = fraction >= 0.5f,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(background)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            ModeTabs(
                selectedMode = state.selectedMode,
                onModeSelect = viewModel::onModeSelect,
                thumbFraction = thumbFraction,
            )
            // Обе области в композиции всегда: Box занимает высоту самой
            // высокой, короткая центрируется; альфа кроссфейдит области
            // синхронно с ползунком, интерактивность — у той, что видна.
            // Активная область рисуется последней (поверх), чтобы тапы
            // не перехватывал угасающий слой.
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                if (fraction < 0.5f) {
                    NightSettings(
                        state = state,
                        viewModel = viewModel,
                        alpha = fraction,
                        enabled = false,
                    )
                    DaySettings(
                        state = state,
                        viewModel = viewModel,
                        alpha = 1f - fraction,
                        enabled = true,
                    )
                } else {
                    DaySettings(
                        state = state,
                        viewModel = viewModel,
                        alpha = 1f - fraction,
                        enabled = false,
                    )
                    NightSettings(
                        state = state,
                        viewModel = viewModel,
                        alpha = fraction,
                        enabled = true,
                    )
                }
            }
        }
    }

    HandleCommands(
        viewModel = viewModel,
        router = router,
    )
}

/**
 * Раздел дневного режима: алфавит, порядок, звук, запуск.
 * Всегда в дневной палитре; плавность переключения даёт [alpha].
 */
@Composable
private fun DaySettings(
    state: LettersSettingsViewModel.State,
    viewModel: LettersSettingsViewModel,
    alpha: Float,
    enabled: Boolean,
) {
    Column(modifier = Modifier.fillMaxWidth().alpha(alpha)) {
        SettingsGroup(
            title = stringResource(R.string.letters_settings_alphabet_group),
            options = state.alphabetOptions,
            onOptionClick = viewModel::onAlphabetSelect,
            nightMode = false,
            enabled = enabled,
        )
        SettingsGroup(
            title = stringResource(R.string.letters_settings_order_group),
            options = state.orderOptions,
            onOptionClick = viewModel::onOrderSelect,
            nightMode = false,
            enabled = enabled,
        )
        SoundSwitchRow(
            title = stringResource(R.string.letters_settings_sound_group),
            checked = state.soundEnabled,
            onCheckedChange = viewModel::onSoundToggle,
            nightMode = false,
            enabled = enabled,
        )
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = DesignSystem.Colors.Letters.ButtonContainerDay,
                contentColor = DesignSystem.Colors.Letters.ButtonContentDay,
            ),
            onClick = viewModel::onRunClick,
        ) {
            Text(text = stringResource(R.string.letters_button_run))
        }
    }
}

/**
 * Раздел ночного режима: алфавит, порядок, таймер, запуск.
 * Всегда в ночной палитре; плавность переключения даёт [alpha].
 */
@Composable
private fun NightSettings(
    state: LettersSettingsViewModel.State,
    viewModel: LettersSettingsViewModel,
    alpha: Float,
    enabled: Boolean,
) {
    Column(modifier = Modifier.fillMaxWidth().alpha(alpha)) {
        SettingsGroup(
            title = stringResource(R.string.letters_settings_alphabet_group),
            options = state.nightAlphabetOptions,
            onOptionClick = viewModel::onNightAlphabetSelect,
            nightMode = true,
            enabled = enabled,
        )
        SettingsGroup(
            title = stringResource(R.string.letters_settings_order_group),
            options = state.nightOrderOptions,
            onOptionClick = viewModel::onNightOrderSelect,
            nightMode = true,
            enabled = enabled,
        )
        SettingsGroup(
            title = stringResource(R.string.letters_night_timer_group),
            options = state.nightDurationOptions,
            onOptionClick = viewModel::onNightDurationSelect,
            nightMode = true,
            enabled = enabled,
        )
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = DesignSystem.Colors.Letters.ButtonContainerNight,
                contentColor = DesignSystem.Colors.Letters.ButtonContentNight,
            ),
            onClick = viewModel::onNightRunClick,
        ) {
            Text(text = stringResource(R.string.letters_button_run_night))
        }
    }
}

@Composable
private fun <T> SettingsGroup(
    title: String,
    options: List<SettingsOptionUiModel<T>>,
    onOptionClick: (T) -> Unit,
    nightMode: Boolean,
    enabled: Boolean,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = textColor(nightMode),
    )
    options.forEach { option ->
        OptionTile(
            text = stringResource(option.labelRes),
            selected = option.selected,
            enabled = enabled,
            nightMode = nightMode,
            onClick = { onOptionClick(option.value) },
        )
    }
}

/** Плитка опции: серый — не выбрана, белый — выбрана. */
@Composable
private fun OptionTile(
    text: String,
    selected: Boolean,
    enabled: Boolean,
    nightMode: Boolean,
    onClick: () -> Unit,
) {
    val background = when {
        selected && nightMode -> DesignSystem.Colors.Letters.OptionTileSelectedNight
        selected -> DesignSystem.Colors.Letters.OptionTileSelectedDay
        nightMode -> DesignSystem.Colors.Letters.OptionTileUnselectedNight
        else -> DesignSystem.Colors.Letters.OptionTileUnselectedDay
    }
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(background)
            // Обработчик вешаем только на активную область: угасающая
            // не должна участвовать во вводе и перехватывать тапы слоя ниже.
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        color = if (selected) {
            DesignSystem.Colors.Letters.OptionTileSelectedText
        } else {
            textColor(nightMode)
        },
    )
}

@Composable
private fun SoundSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    nightMode: Boolean,
    enabled: Boolean,
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
            color = textColor(nightMode),
        )
        Spacer(modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = accentColor(nightMode),
                checkedTrackColor = if (nightMode) {
                    DesignSystem.Colors.Letters.SwitchTrackCheckedNight
                } else {
                    DesignSystem.Colors.Letters.SwitchTrackCheckedDay
                },
            ),
        )
    }
}

/** Тёплый акцент фичи — для радиокнопок и переключателей. */
private fun accentColor(nightMode: Boolean) = if (nightMode) {
    DesignSystem.Colors.Letters.ButtonContainerNight
} else {
    DesignSystem.Colors.Letters.ButtonContainerDay
}

/** Основной цвет текста настроек. */
private fun textColor(nightMode: Boolean) = if (nightMode) {
    DesignSystem.Colors.Letters.TextPrimaryNight
} else {
    DesignSystem.Colors.Letters.TextPrimaryDay
}

@Composable
private fun HandleCommands(
    viewModel: LettersSettingsViewModel,
    router: JoyRouter,
) {
    viewModel.commandsFlow.observe { command ->
        when (command) {
            is Commands.Run -> router.replaceScreen(
                LettersJoyScreen.create(
                    alphabet = command.alphabet,
                    order = command.order,
                ),
            )

            is Commands.RunNight -> router.replaceScreen(
                NightLettersJoyScreen.create(
                    alphabet = command.alphabet,
                    order = command.order,
                    minutes = command.minutes,
                ),
            )
        }
    }
}

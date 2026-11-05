package com.joy.sandbox.feature.letters.impl.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.joy.sandbox.core.navigation.router.JoyRouter
import com.joy.sandbox.core.ui.SandBoxToolbar
import com.joy.sandbox.core.ui.ViewModelFactory
import com.joy.sandbox.feature.letters.api.LettersJoyScreen
import com.joy.sandbox.feature.letters.api.NightLettersJoyScreen
import com.joy.sandbox.feature.letters.impl.R
import com.joy.sandbox.feature.letters.impl.presentation.settings.LettersSettingsViewModel.Commands
import com.joy.sandbox.feature.letters.impl.presentation.settings.model.LettersMode
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

    Scaffold(
        topBar = {
            SandBoxToolbar(
                title = stringResource(R.string.letters_settings_title),
                onBackClick = { router.exit() },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            ModeTabs(
                selectedMode = state.selectedMode,
                onModeSelect = viewModel::onModeSelect,
            )
            when (state.selectedMode) {
                LettersMode.DAY -> DaySettings(
                    state = state,
                    viewModel = viewModel,
                )

                LettersMode.NIGHT -> NightSettings(
                    state = state,
                    viewModel = viewModel,
                )
            }
        }
    }

    HandleCommands(
        viewModel = viewModel,
        router = router,
    )
}

/** Вкладки-режимы: «День» / «Ночь». */
@Composable
private fun ModeTabs(
    selectedMode: LettersMode,
    onModeSelect: (LettersMode) -> Unit,
) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        LettersMode.entries.forEachIndexed { index, mode ->
            SegmentedButton(
                selected = selectedMode == mode,
                onClick = { onModeSelect(mode) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = LettersMode.entries.size,
                ),
            ) {
                Text(text = stringResource(mode.labelRes))
            }
        }
    }
}

/** Раздел дневного режима: алфавит, порядок, звук, запуск. */
@Composable
private fun DaySettings(
    state: LettersSettingsViewModel.State,
    viewModel: LettersSettingsViewModel,
) {
    SettingsGroup(
        title = stringResource(R.string.letters_settings_alphabet_group),
        options = state.alphabetOptions,
        onOptionClick = viewModel::onAlphabetSelect,
    )
    SettingsGroup(
        title = stringResource(R.string.letters_settings_order_group),
        options = state.orderOptions,
        onOptionClick = viewModel::onOrderSelect,
    )
    SoundSwitchRow(
        title = stringResource(R.string.letters_settings_sound_group),
        checked = state.soundEnabled,
        onCheckedChange = viewModel::onSoundToggle,
    )
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        onClick = viewModel::onRunClick,
    ) {
        Text(text = stringResource(R.string.letters_button_run))
    }
}

/** Раздел ночного режима: алфавит, порядок, таймер, запуск. */
@Composable
private fun NightSettings(
    state: LettersSettingsViewModel.State,
    viewModel: LettersSettingsViewModel,
) {
    SettingsGroup(
        title = stringResource(R.string.letters_settings_alphabet_group),
        options = state.nightAlphabetOptions,
        onOptionClick = viewModel::onNightAlphabetSelect,
    )
    SettingsGroup(
        title = stringResource(R.string.letters_settings_order_group),
        options = state.nightOrderOptions,
        onOptionClick = viewModel::onNightOrderSelect,
    )
    SettingsGroup(
        title = stringResource(R.string.letters_night_timer_group),
        options = state.nightDurationOptions,
        onOptionClick = viewModel::onNightDurationSelect,
    )
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        onClick = viewModel::onNightRunClick,
    ) {
        Text(text = stringResource(R.string.letters_button_run_night))
    }
}

@Composable
private fun <T> SettingsGroup(
    title: String,
    options: List<SettingsOptionUiModel<T>>,
    onOptionClick: (T) -> Unit,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
    )
    options.forEach { option ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOptionClick(option.value) },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(
                selected = option.selected,
                onClick = null,
            )
            Text(
                modifier = Modifier.padding(start = 8.dp),
                text = stringResource(option.labelRes),
            )
        }
    }
}

@Composable
private fun SoundSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
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
        )
        Spacer(modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
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

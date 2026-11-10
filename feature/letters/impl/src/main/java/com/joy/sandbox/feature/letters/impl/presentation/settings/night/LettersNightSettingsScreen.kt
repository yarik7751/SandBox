package com.joy.sandbox.feature.letters.impl.presentation.settings.night

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.joy.sandbox.core.navigation.router.JoyRouter
import com.joy.sandbox.core.ui.DesignSystem
import com.joy.sandbox.core.ui.SandBoxToolbar
import com.joy.sandbox.core.ui.SystemBarColors
import com.joy.sandbox.core.ui.ViewModelFactory
import com.joy.sandbox.feature.letters.api.NightLettersJoyScreen
import com.joy.sandbox.feature.letters.impl.R
import com.joy.sandbox.feature.letters.impl.presentation.ImmersiveModeEffect
import com.joy.sandbox.feature.letters.impl.presentation.settings.NightSettingsPalette
import com.joy.sandbox.feature.letters.impl.presentation.settings.SettingsGroup
import com.joy.sandbox.feature.letters.impl.presentation.settings.night.LettersNightSettingsViewModel.Commands
import com.joy.sandbox.utils.observe

/**
 * Ночные настройки фичи «Буквы»: алфавит, порядок показа, таймер.
 * Экран всегда в тёмной палитре. «Запустить» открывает ночной показ
 * через replace — настройки выпадают из стека, возврат с экрана
 * показа попадает сразу на главный.
 */
@Composable
fun LettersNightSettingsScreen(
    viewModelFactory: ViewModelFactory,
    router: JoyRouter,
) {
    val viewModel: LettersNightSettingsViewModel = viewModel(factory = viewModelFactory)
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    ImmersiveModeEffect()

    val toolbarBackground = DesignSystem.Colors.Letters.ToolbarBackgroundNight
    val background = DesignSystem.Colors.Letters.SettingsBackgroundNight

    Scaffold(
        topBar = {
            SandBoxToolbar(
                title = stringResource(R.string.letters_night_settings_title),
                onBackClick = { router.exit() },
                backgroundColor = toolbarBackground,
                contentColor = DesignSystem.Colors.Letters.ToolbarContentNight,
            )
        },
    ) { padding ->
        // Status bar — в цвет тулбара, navigation bar — в цвет фона.
        SystemBarColors(
            statusBarColor = toolbarBackground,
            navigationBarColor = background,
            lightIcons = true,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(background)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            SettingsGroup(
                title = stringResource(R.string.letters_settings_alphabet_group),
                options = state.alphabetOptions,
                onOptionClick = viewModel::onAlphabetSelect,
                palette = NightSettingsPalette,
            )
            SettingsGroup(
                title = stringResource(R.string.letters_settings_order_group),
                options = state.orderOptions,
                onOptionClick = viewModel::onOrderSelect,
                palette = NightSettingsPalette,
            )
            SettingsGroup(
                title = stringResource(R.string.letters_night_timer_group),
                options = state.durationOptions,
                onOptionClick = viewModel::onDurationSelect,
                palette = NightSettingsPalette,
            )
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DesignSystem.Colors.Letters.ButtonContainerNight,
                    contentColor = DesignSystem.Colors.Letters.ButtonContentNight,
                ),
                onClick = viewModel::onRunClick,
            ) {
                Text(text = stringResource(R.string.letters_button_run_night))
            }
        }
    }

    HandleCommands(
        viewModel = viewModel,
        router = router,
    )
}

@Composable
private fun HandleCommands(
    viewModel: LettersNightSettingsViewModel,
    router: JoyRouter,
) {
    viewModel.commandsFlow.observe { command ->
        when (command) {
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

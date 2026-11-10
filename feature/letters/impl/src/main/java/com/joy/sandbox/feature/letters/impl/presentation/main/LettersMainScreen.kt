package com.joy.sandbox.feature.letters.impl.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.joy.sandbox.core.navigation.router.JoyRouter
import com.joy.sandbox.core.ui.DesignSystem
import com.joy.sandbox.core.ui.SystemBarColors
import com.joy.sandbox.core.ui.ViewModelFactory
import com.joy.sandbox.feature.letters.api.LettersDaySettingsJoyScreen
import com.joy.sandbox.feature.letters.api.LettersNightSettingsJoyScreen
import com.joy.sandbox.feature.letters.impl.R
import com.joy.sandbox.feature.letters.impl.presentation.ImmersiveModeEffect
import com.joy.sandbox.feature.letters.impl.presentation.LettersScreenTitle
import com.joy.sandbox.feature.letters.impl.presentation.main.LettersMainViewModel.Commands
import com.joy.sandbox.feature.letters.impl.presentation.settings.DaySettingsPalette
import com.joy.sandbox.feature.letters.impl.presentation.settings.SettingsButton
import com.joy.sandbox.utils.observe

/**
 * Главный экран фичи «Буквы»: кнопка «Буквы» открывает дневные
 * настройки → показ букв, кнопка «Ночник» — ночные настройки
 * → ночной показ.
 */
@Composable
fun LettersMainScreen(
    viewModelFactory: ViewModelFactory,
    router: JoyRouter,
) {
    val viewModel: LettersMainViewModel = viewModel(factory = viewModelFactory)

    ImmersiveModeEffect()

    val background = DesignSystem.Colors.Letters.SettingsBackgroundDay

    // Status и navigation bar — в цвет фона (тулбара нет).
    SystemBarColors(
        statusBarColor = background,
        navigationBarColor = background,
        lightIcons = false,
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .verticalScroll(rememberScrollState()),
    ) {
        LettersScreenTitle(
            text = stringResource(R.string.letters_main_title),
            color = DaySettingsPalette.text,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                SettingsButton(
                    text = stringResource(R.string.letters_main_button_day),
                    palette = DaySettingsPalette,
                    onClick = viewModel::onDayClick,
                )
                SettingsButton(
                    text = stringResource(R.string.letters_main_button_night),
                    palette = DaySettingsPalette,
                    onClick = viewModel::onNightClick,
                )
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
    viewModel: LettersMainViewModel,
    router: JoyRouter,
) {
    viewModel.commandsFlow.observe { command ->
        when (command) {
            Commands.OpenDaySettings -> router.navigateTo(LettersDaySettingsJoyScreen)
            Commands.OpenNightSettings -> router.navigateTo(LettersNightSettingsJoyScreen)
        }
    }
}

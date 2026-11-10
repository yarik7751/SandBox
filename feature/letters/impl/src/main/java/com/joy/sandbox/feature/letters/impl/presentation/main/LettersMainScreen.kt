package com.joy.sandbox.feature.letters.impl.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.joy.sandbox.core.navigation.router.JoyRouter
import com.joy.sandbox.core.ui.DesignSystem
import com.joy.sandbox.core.ui.SandBoxToolbar
import com.joy.sandbox.core.ui.SystemBarColors
import com.joy.sandbox.core.ui.ViewModelFactory
import com.joy.sandbox.feature.letters.api.LettersDaySettingsJoyScreen
import com.joy.sandbox.feature.letters.api.LettersNightSettingsJoyScreen
import com.joy.sandbox.feature.letters.impl.R
import com.joy.sandbox.feature.letters.impl.presentation.main.LettersMainViewModel.Commands
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

    Scaffold(
        topBar = {
            SandBoxToolbar(
                title = stringResource(R.string.letters_main_title),
                onBackClick = { router.exit() },
                backgroundColor = DesignSystem.Colors.Letters.ToolbarBackgroundDay,
                contentColor = DesignSystem.Colors.Letters.ToolbarContentDay,
            )
        },
    ) { padding ->
        // Status bar — в цвет тулбара, navigation bar — в цвет фона,
        // как на дневных настройках: переход между экранами без смены
        // цвета системных баров.
        SystemBarColors(
            statusBarColor = DesignSystem.Colors.Letters.ToolbarBackgroundDay,
            navigationBarColor = DesignSystem.Colors.Letters.SettingsBackgroundDay,
            lightIcons = false,
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DesignSystem.Colors.Letters.SettingsBackgroundDay)
                .padding(padding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Button(
                    modifier = Modifier.padding(top = 8.dp),
                    onClick = viewModel::onDayClick,
                ) {
                    Text(text = stringResource(R.string.letters_main_button_day))
                }
                Button(
                    modifier = Modifier.padding(top = 8.dp),
                    onClick = viewModel::onNightClick,
                ) {
                    Text(text = stringResource(R.string.letters_main_button_night))
                }
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

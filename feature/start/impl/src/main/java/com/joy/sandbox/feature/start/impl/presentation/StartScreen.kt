package com.joy.sandbox.feature.start.impl.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.joy.sandbox.core.navigation.router.JoyRouter
import com.joy.sandbox.core.ui.SandBoxToolbar
import com.joy.sandbox.core.ui.ViewModelFactory
import com.joy.sandbox.feature.start.impl.R
import com.joy.sandbox.feature.home.api.HomeJoyScreen
import com.joy.sandbox.feature.start.impl.presentation.StartViewModel.Commands
import com.joy.sandbox.utils.observe

@Composable
fun StartScreen(
    viewModelFactory: ViewModelFactory,
    router: JoyRouter,
) {
    val viewModel: StartViewModel = viewModel(factory = viewModelFactory)

    // Здесь происходит инициализация фичей и настроек приложения
    LaunchedEffect(Unit) {
        viewModel.onInitialized()
    }

    Scaffold(
        topBar = {
            // Корневой экран стека — стрелка назад не нужна
            SandBoxToolbar(title = stringResource(R.string.start_screen_title))
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
        }
    }

    HandleCommands(
        viewModel = viewModel,
        router = router,
    )
}

@Composable
private fun HandleCommands(
    viewModel: StartViewModel,
    router: JoyRouter,
) {
    viewModel.commandsFlow.observe { command ->
        when (command) {
            Commands.OpenHome -> {
                router.replaceScreen(HomeJoyScreen)
            }
        }
    }
}

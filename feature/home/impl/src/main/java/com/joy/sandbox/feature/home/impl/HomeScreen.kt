package com.joy.sandbox.feature.home.impl

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
import com.joy.sandbox.core.ui.SandBoxToolbar
import com.joy.sandbox.core.ui.ViewModelFactory
import com.joy.sandbox.feature.home.impl.HomeViewModel.Commands
import com.joy.sandbox.feature.websocket.api.WebSocketJoyScreen
import com.joy.sandbox.utils.observe

/**
 * Экран с кнопками фичей. Каждая фича добавляет сюда кнопку
 * с навигацией на свой экран.
 */
@Composable
fun HomeScreen(
    viewModelFactory: ViewModelFactory,
    router: JoyRouter,
) {
    val viewModel: HomeViewModel = viewModel(factory = viewModelFactory)

    Scaffold(
        topBar = {
            // Корневой экран после replace — стрелка назад не нужна
            SandBoxToolbar(title = stringResource(R.string.home_title))
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
                    onClick = viewModel::onWebSocketClick,
                ) {
                    Text(text = stringResource(R.string.home_button_websocket))
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
    viewModel: HomeViewModel,
    router: JoyRouter,
) {
    viewModel.commandsFlow.observe { command ->
        when (command) {
            Commands.OpenWebSocket -> router.navigateTo(WebSocketJoyScreen)
        }
    }
}

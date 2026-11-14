package com.joy.sandbox.feature.dungeon.impl.presentation.title

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.joy.sandbox.core.navigation.router.JoyRouter
import com.joy.sandbox.core.ui.ViewModelFactory
import com.joy.sandbox.feature.dungeon.api.DungeonGameJoyScreen
import com.joy.sandbox.feature.dungeon.api.DungeonLevelsJoyScreen
import com.joy.sandbox.feature.dungeon.impl.R
import com.joy.sandbox.feature.dungeon.impl.presentation.theme.DungeonColors
import com.joy.sandbox.feature.dungeon.impl.presentation.theme.DungeonTheme
import com.joy.sandbox.feature.dungeon.impl.presentation.title.DungeonTitleViewModel.Commands
import com.joy.sandbox.utils.observe

/** Титульный экран «Подземелья» с кнопками «Начать»/«Уровни». */
@Composable
fun DungeonTitleScreen(
    viewModelFactory: ViewModelFactory,
    router: JoyRouter,
) {
    val viewModel: DungeonTitleViewModel = viewModel(factory = viewModelFactory)

    DungeonTheme {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = stringResource(R.string.dungeon_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = DungeonColors.GoldBright,
                )
                Text(
                    text = stringResource(R.string.dungeon_title_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = DungeonColors.TextDim,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
                )
                Button(onClick = viewModel::onStartClick) {
                    Text(stringResource(R.string.dungeon_start))
                }
                TextButton(onClick = viewModel::onLevelsClick) {
                    Text(stringResource(R.string.dungeon_levels))
                }
            }
        }
    }

    HandleCommands(viewModel = viewModel, router = router)
}

@Composable
private fun HandleCommands(
    viewModel: DungeonTitleViewModel,
    router: JoyRouter,
) {
    viewModel.commandsFlow.observe { command ->
        when (command) {
            is Commands.OpenGame -> router.navigateTo(
                DungeonGameJoyScreen.create(command.levelId.toLong()),
            )

            Commands.OpenLevels -> router.navigateTo(DungeonLevelsJoyScreen)
        }
    }
}

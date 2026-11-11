package com.joy.sandbox.feature.dungeon.impl.presentation.title

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.joy.sandbox.core.navigation.router.JoyRouter
import com.joy.sandbox.core.ui.SandBoxToolbar
import com.joy.sandbox.core.ui.ViewModelFactory
import com.joy.sandbox.feature.dungeon.api.DungeonIntroJoyScreen
import com.joy.sandbox.feature.dungeon.impl.R
import com.joy.sandbox.feature.dungeon.impl.presentation.model.DungeonTitleUiModel
import com.joy.sandbox.feature.dungeon.impl.presentation.model.LevelItemUiModel
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
    val model by viewModel.stateFlow.collectAsStateWithLifecycle()

    DungeonTheme {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                SandBoxToolbar(
                    title = stringResource(R.string.dungeon_title),
                    onBackClick = viewModel::onBackClick,
                )
            },
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
                TextButton(onClick = viewModel::onLevelsToggle) {
                    Text(stringResource(R.string.dungeon_levels))
                }
                if (model.showLevels) {
                    LevelList(
                        model = model,
                        onLevelClick = viewModel::onLevelClick,
                    )
                }
            }
        }
    }

    HandleCommands(viewModel = viewModel, router = router)
}

@Composable
private fun LevelList(
    model: DungeonTitleUiModel,
    onLevelClick: (Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        model.levels.forEach { level ->
            TextButton(
                onClick = { onLevelClick(level.id) },
                enabled = !level.locked,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = levelLine(level),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun levelLine(level: LevelItemUiModel): String =
    if (level.locked) {
        "${level.id}. ${level.name} — " + stringResource(R.string.dungeon_level_locked)
    } else {
        val best = level.bestTurns
        if (best != null) {
            "${level.id}. ${level.name} · " +
                stringResource(R.string.dungeon_level_par, level.par) + " · " +
                stringResource(R.string.dungeon_level_best, best)
        } else {
            "${level.id}. ${level.name} · " + stringResource(R.string.dungeon_level_par, level.par)
        }
    }

@Composable
private fun HandleCommands(
    viewModel: DungeonTitleViewModel,
    router: JoyRouter,
) {
    viewModel.commandsFlow.observe { command ->
        when (command) {
            is Commands.OpenIntro -> router.navigateTo(
                DungeonIntroJoyScreen.create(command.levelId.toLong()),
            )
            Commands.Exit -> router.exit()
        }
    }
}

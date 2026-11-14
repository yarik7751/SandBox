package com.joy.sandbox.feature.dungeon.impl.presentation.levels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.joy.sandbox.core.navigation.router.JoyRouter
import com.joy.sandbox.core.ui.ViewModelFactory
import com.joy.sandbox.feature.dungeon.api.DungeonIntroJoyScreen
import com.joy.sandbox.feature.dungeon.impl.R
import com.joy.sandbox.feature.dungeon.impl.presentation.levels.DungeonLevelsViewModel.Commands
import com.joy.sandbox.feature.dungeon.impl.presentation.model.LevelItemUiModel
import com.joy.sandbox.feature.dungeon.impl.presentation.theme.DungeonColors
import com.joy.sandbox.feature.dungeon.impl.presentation.theme.DungeonTheme
import com.joy.sandbox.utils.observe

/** Экран выбора уровня подземелья: LazyColumn, готов к большому числу уровней. */
@Composable
fun DungeonLevelsScreen(
    viewModelFactory: ViewModelFactory,
    router: JoyRouter,
) {
    val viewModel: DungeonLevelsViewModel = viewModel(factory = viewModelFactory)
    val model by viewModel.stateFlow.collectAsStateWithLifecycle()

    DungeonTheme {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                item {
                    Text(
                        text = stringResource(R.string.dungeon_levels_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = DungeonColors.GoldBright,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                    )
                }
                items(model.levels, key = { it.id }) { level ->
                    TextButton(
                        onClick = { viewModel.onLevelClick(level.id) },
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
    }

    HandleCommands(viewModel = viewModel, router = router)
}

@Composable
private fun levelLine(level: LevelItemUiModel): String =
    if (level.locked) {
        "${level.id}. ${level.name} — " + stringResource(R.string.dungeon_level_locked)
    } else {
        val best = level.bestTurns
        if (best != null) {
            "${level.id}. ${level.name} · " + stringResource(R.string.dungeon_level_best, best)
        } else {
            "${level.id}. ${level.name}"
        }
    }

@Composable
private fun HandleCommands(
    viewModel: DungeonLevelsViewModel,
    router: JoyRouter,
) {
    viewModel.commandsFlow.observe { command ->
        when (command) {
            is Commands.OpenIntro -> router.navigateTo(
                DungeonIntroJoyScreen.create(command.levelId.toLong()),
            )
        }
    }
}

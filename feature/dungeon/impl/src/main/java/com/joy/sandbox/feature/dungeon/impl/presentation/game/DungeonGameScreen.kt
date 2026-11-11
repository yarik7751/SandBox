package com.joy.sandbox.feature.dungeon.impl.presentation.game

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.joy.sandbox.core.dungeon.model.DungeonLevels
import com.joy.sandbox.core.navigation.currentNavLongArgument
import com.joy.sandbox.core.navigation.router.JoyRouter
import com.joy.sandbox.core.ui.SandBoxToolbar
import com.joy.sandbox.core.ui.ViewModelFactory
import com.joy.sandbox.feature.dungeon.api.DungeonGameJoyScreen
import com.joy.sandbox.feature.dungeon.api.DungeonIntroJoyScreen
import com.joy.sandbox.feature.dungeon.api.DungeonJoyScreen
import com.joy.sandbox.feature.dungeon.impl.R
import com.joy.sandbox.feature.dungeon.impl.presentation.components.DungeonDialog
import com.joy.sandbox.feature.dungeon.impl.presentation.components.DungeonDialogNote
import com.joy.sandbox.feature.dungeon.impl.presentation.components.DungeonMenuButton
import com.joy.sandbox.feature.dungeon.impl.presentation.game.DungeonGameViewModel.Commands
import com.joy.sandbox.feature.dungeon.impl.presentation.model.DungeonGameUiModel
import com.joy.sandbox.feature.dungeon.impl.presentation.theme.DungeonColors
import com.joy.sandbox.feature.dungeon.impl.presentation.theme.DungeonTheme
import com.joy.sandbox.utils.observe

/**
 * Игровой экран «Подземелья»: доска, счётчики, Отмена/Заново/Пауза/Звук
 * и диалоги паузы, победы, поражения и финала.
 */
@Composable
fun DungeonGameScreen(
    viewModelFactory: ViewModelFactory,
    router: JoyRouter,
) {
    val levelId = currentNavLongArgument(DungeonGameJoyScreen.ARG_LEVEL_ID)?.toInt()
    val viewModel: DungeonGameViewModel = viewModel(factory = viewModelFactory)
    val model by viewModel.stateFlow.collectAsStateWithLifecycle()
    var showPause by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(levelId) { levelId?.let(viewModel::init) }

    BackHandler(enabled = model?.over == null) { showPause = true }

    DungeonTheme {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                SandBoxToolbar(
                    title = model?.levelName ?: stringResource(R.string.dungeon_title),
                    onBackClick = { router.exit() },
                )
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                model?.let { state ->
                    StatsRow(state)
                    DungeonBoard(
                        model = state,
                        exitLabel = stringResource(R.string.dungeon_exit_label),
                        enemyLabel = stringResource(R.string.dungeon_enemy_label),
                        onRoomClick = viewModel::onRoomClick,
                        onDoorClick = viewModel::onDoorClick,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = BOARD_PADDING)
                            .padding(bottom = BOARD_PADDING),
                    )
                    ControlsRow(
                        state = state,
                        onUndo = viewModel::onUndoClick,
                        onRestart = viewModel::onRestartClick,
                        onPause = { showPause = true },
                        onSoundToggle = { viewModel.onFeedbackToggle(sound = true) },
                    )
                }
            }
        }
    }

    GameDialogs(
        model = model,
        showPause = showPause,
        onDismissPause = { showPause = false },
        viewModel = viewModel,
        router = router,
    )
}

@Composable
private fun StatsRow(model: DungeonGameUiModel) {
    val hostagesLeft = model.hostages.count { !it.exited }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.dungeon_level_number, model.levelId),
            style = MaterialTheme.typography.headlineSmall,
            color = DungeonColors.GoldBright,
        )
        Text(
            text = stringResource(R.string.dungeon_turns_count, model.turns),
            style = MaterialTheme.typography.titleSmall,
            color = DungeonColors.TextDim,
        )
        Text(
            text = stringResource(R.string.dungeon_hostages_left, hostagesLeft),
            style = MaterialTheme.typography.titleSmall,
            color = DungeonColors.TextDim,
        )
    }
}

@Composable
private fun ControlsRow(
    state: DungeonGameUiModel,
    onUndo: () -> Unit,
    onRestart: () -> Unit,
    onPause: () -> Unit,
    onSoundToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        TextButton(onClick = onUndo, enabled = state.canUndo) {
            Text(stringResource(R.string.dungeon_undo))
        }
        TextButton(onClick = onRestart, enabled = state.over == null) {
            Text(stringResource(R.string.dungeon_restart))
        }
        TextButton(onClick = onPause, enabled = state.over == null) {
            Text(stringResource(R.string.dungeon_pause))
        }
        TextButton(onClick = onSoundToggle) {
            Text(
                stringResource(
                    if (state.soundEnabled) R.string.dungeon_sound_on else R.string.dungeon_sound_off,
                ),
            )
        }
    }
}

@Composable
private fun GameDialogs(
    model: DungeonGameUiModel?,
    showPause: Boolean,
    onDismissPause: () -> Unit,
    viewModel: DungeonGameViewModel,
    router: JoyRouter,
) {
    val over = model?.over
    val isLastLevel = model?.levelId == DungeonLevels.ALL.size
    when {
        showPause && over == null -> PauseDialog(
            model = model,
            onContinue = onDismissPause,
            viewModel = viewModel,
        )

        over?.win == true && isLastLevel -> FinalDialog(
            onToTitle = {
                viewModel.onExitToTitle()
            },
        )

        over?.win == true -> WinDialog(
            model = model,
            viewModel = viewModel,
        )

        over != null -> LoseDialog(
            model = model,
            viewModel = viewModel,
        )
    }

    HandleCommands(viewModel = viewModel, router = router)
}

@Composable
private fun PauseDialog(
    model: DungeonGameUiModel?,
    onContinue: () -> Unit,
    viewModel: DungeonGameViewModel,
) {
    DungeonDialog(
        title = stringResource(R.string.dungeon_pause),
        onDismiss = onContinue,
    ) {
        DungeonMenuButton(
            text = stringResource(R.string.dungeon_continue),
            primary = true,
            onClick = onContinue,
        )
        DungeonMenuButton(
            text = stringResource(
                if (model?.soundEnabled == true) R.string.dungeon_sound_on else R.string.dungeon_sound_off,
            ),
            onClick = { viewModel.onFeedbackToggle(sound = true) },
        )
        DungeonMenuButton(
            text = stringResource(
                if (model?.vibrationEnabled == true) {
                    R.string.dungeon_vibration_on
                } else {
                    R.string.dungeon_vibration_off
                },
            ),
            onClick = { viewModel.onFeedbackToggle(sound = false) },
        )
        DungeonMenuButton(
            text = stringResource(R.string.dungeon_restart),
            onClick = viewModel::onRestartClick,
        )
        DungeonMenuButton(
            text = stringResource(R.string.dungeon_to_title),
            onClick = viewModel::onExitToTitle,
        )
    }
}

@Composable
private fun WinDialog(
    model: DungeonGameUiModel,
    viewModel: DungeonGameViewModel,
) {
    DungeonDialog(
        title = stringResource(R.string.dungeon_win_title),
        onDismiss = null,
    ) {
        DungeonDialogNote(text = stringResource(R.string.dungeon_win_stats, model.turns, model.par))
        if (model.over?.newRecord == true) {
            Text(
                text = stringResource(R.string.dungeon_new_record),
                style = MaterialTheme.typography.titleSmall,
                color = DungeonColors.Gold,
            )
        }
        DungeonMenuButton(
            text = stringResource(R.string.dungeon_next),
            primary = true,
            onClick = viewModel::onNextClick,
        )
        DungeonMenuButton(
            text = stringResource(R.string.dungeon_again),
            onClick = viewModel::onRestartClick,
        )
        DungeonMenuButton(
            text = stringResource(R.string.dungeon_to_title),
            onClick = viewModel::onExitToTitle,
        )
    }
}

@Composable
private fun LoseDialog(
    model: DungeonGameUiModel,
    viewModel: DungeonGameViewModel,
) {
    DungeonDialog(
        title = stringResource(R.string.dungeon_lose_title),
        onDismiss = null,
    ) {
        Text(
            text = model.over?.reason.orEmpty(),
            style = MaterialTheme.typography.bodyLarge,
        )
        DungeonMenuButton(
            text = stringResource(R.string.dungeon_again),
            primary = true,
            onClick = viewModel::onRestartClick,
        )
        DungeonMenuButton(
            text = stringResource(R.string.dungeon_to_title),
            onClick = viewModel::onExitToTitle,
        )
    }
}

@Composable
private fun FinalDialog(onToTitle: () -> Unit) {
    DungeonDialog(
        title = stringResource(R.string.dungeon_final_title),
        onDismiss = null,
    ) {
        Text(
            text = stringResource(R.string.dungeon_final_text),
            style = MaterialTheme.typography.bodyLarge,
        )
        DungeonMenuButton(
            text = stringResource(R.string.dungeon_to_title),
            primary = true,
            onClick = onToTitle,
        )
    }
}

@Composable
private fun HandleCommands(
    viewModel: DungeonGameViewModel,
    router: JoyRouter,
) {
    viewModel.commandsFlow.observe { command ->
        when (command) {
            is Commands.OpenNext -> router.navigateTo(DungeonIntroJoyScreen.create(command.levelId.toLong()))
            Commands.ExitToTitle -> router.backTo(DungeonJoyScreen)
        }
    }
}

private val BOARD_PADDING = 16.dp

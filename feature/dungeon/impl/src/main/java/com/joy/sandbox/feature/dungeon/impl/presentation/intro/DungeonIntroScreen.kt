package com.joy.sandbox.feature.dungeon.impl.presentation.intro

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.joy.sandbox.core.dungeon.model.DungeonMechanic
import com.joy.sandbox.core.navigation.currentNavLongArgument
import com.joy.sandbox.core.navigation.router.JoyRouter
import com.joy.sandbox.core.ui.ViewModelFactory
import com.joy.sandbox.feature.dungeon.api.DungeonGameJoyScreen
import com.joy.sandbox.feature.dungeon.api.DungeonIntroJoyScreen
import com.joy.sandbox.feature.dungeon.impl.R
import com.joy.sandbox.feature.dungeon.impl.presentation.intro.DungeonIntroViewModel.Commands
import com.joy.sandbox.feature.dungeon.impl.presentation.theme.DungeonColors
import com.joy.sandbox.feature.dungeon.impl.presentation.theme.DungeonTheme
import com.joy.sandbox.utils.observe

/**
 * Интро уровня: история уровня и карточки новых механик — перед входом
 * в подземелье. Экран показывается только когда у уровня есть механики,
 * которые игрок ещё не читал (иначе ViewModel сразу уводит в игру).
 */
@Composable
fun DungeonIntroScreen(
    viewModelFactory: ViewModelFactory,
    router: JoyRouter,
) {
    val levelId = currentNavLongArgument(DungeonIntroJoyScreen.ARG_LEVEL_ID)?.toInt()
    val viewModel: DungeonIntroViewModel = viewModel(factory = viewModelFactory)
    val model by viewModel.stateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(levelId) { levelId?.let(viewModel::init) }

    DungeonTheme {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                model?.let { state ->
                    Text(
                        text = "${state.levelId}. ${state.levelName}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = DungeonColors.GoldBright,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = state.intro,
                        style = MaterialTheme.typography.bodyLarge,
                        color = DungeonColors.Text,
                        modifier = Modifier.padding(top = 16.dp),
                    )
                    state.mechanics.forEach { mechanic ->
                        MechanicCard(
                            mechanic = mechanic,
                            modifier = Modifier.padding(top = 16.dp),
                        )
                    }
                    Text(
                        text = stringResource(R.string.dungeon_intro_goal),
                        style = MaterialTheme.typography.bodyMedium,
                        color = DungeonColors.Gold,
                        modifier = Modifier.padding(top = 16.dp),
                    )
                    Button(
                        onClick = viewModel::onEnterClick,
                        modifier = Modifier.padding(top = 24.dp),
                    ) {
                        Text(stringResource(R.string.dungeon_enter))
                    }
                }
            }
        }
    }

    HandleCommands(viewModel = viewModel, router = router)
}

/** Карточка новой механики: название и описание из строковых ресурсов. */
@Composable
private fun MechanicCard(
    mechanic: DungeonMechanic,
    modifier: Modifier = Modifier,
) {
    val title: String
    val description: String
    when (mechanic) {
        DungeonMechanic.BASE -> {
            title = stringResource(R.string.dungeon_mechanic_base_title)
            description = stringResource(R.string.dungeon_mechanic_base_desc)
        }

        DungeonMechanic.JAMMED -> {
            title = stringResource(R.string.dungeon_mechanic_jammed_title)
            description = stringResource(R.string.dungeon_mechanic_jammed_desc)
        }

        DungeonMechanic.CONCEALED -> {
            title = stringResource(R.string.dungeon_mechanic_concealed_title)
            description = stringResource(R.string.dungeon_mechanic_concealed_desc)
        }

        DungeonMechanic.BLOCKED -> {
            title = stringResource(R.string.dungeon_mechanic_blocked_title)
            description = stringResource(R.string.dungeon_mechanic_blocked_desc)
        }

        DungeonMechanic.TOGGLES_BUDGET -> {
            title = stringResource(R.string.dungeon_mechanic_toggles_budget_title)
            description = stringResource(R.string.dungeon_mechanic_toggles_budget_desc)
        }

        DungeonMechanic.ENEMY_DOOR_CHARGE -> {
            title = stringResource(R.string.dungeon_mechanic_enemy_door_charge_title)
            description = stringResource(R.string.dungeon_mechanic_enemy_door_charge_desc)
        }

        DungeonMechanic.EXTRA_MOVES -> {
            title = stringResource(R.string.dungeon_mechanic_extra_moves_title)
            description = stringResource(R.string.dungeon_mechanic_extra_moves_desc)
        }
    }
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = DungeonColors.Gold,
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = DungeonColors.Text,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun HandleCommands(
    viewModel: DungeonIntroViewModel,
    router: JoyRouter,
) {
    viewModel.commandsFlow.observe { command ->
        when (command) {
            is Commands.OpenGame -> router.navigateTo(
                DungeonGameJoyScreen.create(command.levelId.toLong()),
            )
        }
    }
}

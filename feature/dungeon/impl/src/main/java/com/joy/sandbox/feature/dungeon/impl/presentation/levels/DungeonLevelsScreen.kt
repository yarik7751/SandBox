package com.joy.sandbox.feature.dungeon.impl.presentation.levels

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.joy.sandbox.core.navigation.router.JoyRouter
import com.joy.sandbox.core.ui.ViewModelFactory
import com.joy.sandbox.feature.dungeon.api.DungeonGameJoyScreen
import com.joy.sandbox.feature.dungeon.impl.R
import com.joy.sandbox.feature.dungeon.impl.presentation.levels.DungeonLevelsViewModel.Commands
import com.joy.sandbox.feature.dungeon.impl.presentation.model.LevelItemUiModel
import com.joy.sandbox.feature.dungeon.impl.presentation.theme.DungeonColors
import com.joy.sandbox.feature.dungeon.impl.presentation.theme.DungeonTheme
import com.joy.sandbox.utils.observe

/** Экран выбора уровня подземелья: сетка кругов, 5 в ряд. */
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
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
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
                    LevelCircle(
                        level = level,
                        onClick = { viewModel.onLevelClick(level.id) },
                    )
                }
            }
        }
    }

    HandleCommands(viewModel = viewModel, router = router)
}

@Composable
private fun LevelCircle(
    level: LevelItemUiModel,
    onClick: () -> Unit,
) {
    val passed = !level.locked && level.bestTurns != null
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .then(
                if (passed) {
                    Modifier.background(DungeonColors.Gold)
                } else {
                    Modifier.border(width = 2.dp, color = DungeonColors.Gold, shape = CircleShape)
                }
            )
            .clickable(enabled = !level.locked, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = level.id.toString(),
            style = MaterialTheme.typography.titleLarge,
            color = if (passed) DungeonColors.OnGold else DungeonColors.Gold,
        )
    }
}

@Composable
private fun HandleCommands(
    viewModel: DungeonLevelsViewModel,
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

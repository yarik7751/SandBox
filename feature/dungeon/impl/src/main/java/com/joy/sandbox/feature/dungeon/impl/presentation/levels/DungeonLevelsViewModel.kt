package com.joy.sandbox.feature.dungeon.impl.presentation.levels

import androidx.lifecycle.viewModelScope
import com.joy.sandbox.core.ui.BaseViewModel
import com.joy.sandbox.core.ui.CommandFlow
import com.joy.sandbox.feature.dungeon.impl.domain.DungeonLevelsInteractor
import com.joy.sandbox.feature.dungeon.impl.domain.DungeonProgressRepository
import com.joy.sandbox.feature.dungeon.impl.presentation.model.DungeonLevelsUiModel
import com.joy.sandbox.feature.dungeon.impl.presentation.model.LevelItemUiModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * Экран выбора уровня: лента со всеми локами (рукотворные + пак из assets)
 * и рекордами. Замки и рекорды подтягиваются из потока прогресса — лента
 * обновляется сама после победы и возврата с игрового экрана.
 */
class DungeonLevelsViewModel @Inject constructor(
    private val progressRepository: DungeonProgressRepository,
    private val levelsInteractor: DungeonLevelsInteractor,
) : BaseViewModel() {

    val stateFlow = MutableStateFlow(DungeonLevelsUiModel(levels = emptyList()))

    val commandsFlow = CommandFlow<Commands>(viewModelScope)

    sealed interface Commands {

        data class OpenGame(val levelId: Int) : Commands
    }

    init {
        loadProgress()
    }

    fun onLevelClick(levelId: Int) {
        val level = stateFlow.value.levels.firstOrNull { it.id == levelId } ?: return
        if (!level.locked) commandsFlow.tryEmit(Commands.OpenGame(levelId))
    }

    private fun loadProgress() {
        viewModelScope.launch {
            progressRepository.observe().collect { progress ->
                stateFlow.value = DungeonLevelsUiModel(
                    levels = levelsInteractor.levels()
                        .map { level ->
                            LevelItemUiModel(
                                id = level.id,
                                name = level.name,
                                locked = level.id > progress.unlockedLevel,
                                current = level.id == progress.unlockedLevel,
                                bestTurns = progress.bestTurns[level.id],
                            )
                        },
                )
            }
        }
    }
}

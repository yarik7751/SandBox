package com.joy.sandbox.feature.dungeon.impl.presentation.levels

import androidx.lifecycle.viewModelScope
import com.joy.sandbox.core.dungeon.model.DungeonLevels
import com.joy.sandbox.core.ui.BaseViewModel
import com.joy.sandbox.core.ui.CommandFlow
import com.joy.sandbox.feature.dungeon.impl.domain.DungeonProgressRepository
import com.joy.sandbox.feature.dungeon.impl.presentation.model.DungeonLevelsUiModel
import com.joy.sandbox.feature.dungeon.impl.presentation.model.LevelItemUiModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/** Экран выбора уровня: список с локами и рекордами. */
class DungeonLevelsViewModel @Inject constructor(
    private val progressRepository: DungeonProgressRepository,
) : BaseViewModel() {

    val stateFlow = MutableStateFlow(DungeonLevelsUiModel(levels = emptyList()))

    val commandsFlow = CommandFlow<Commands>(viewModelScope)

    sealed interface Commands {

        data class OpenIntro(val levelId: Int) : Commands
    }

    init {
        loadProgress()
    }

    fun onLevelClick(levelId: Int) {
        val level = stateFlow.value.levels.firstOrNull { it.id == levelId } ?: return
        if (!level.locked) commandsFlow.tryEmit(Commands.OpenIntro(levelId))
    }

    private fun loadProgress() {
        viewModelScope.launch {
            val progress = progressRepository.load()
            stateFlow.value = DungeonLevelsUiModel(
                levels = DungeonLevels.ALL.map { level ->
                    LevelItemUiModel(
                        id = level.id,
                        name = level.name,
                        par = level.par,
                        locked = level.id > progress.unlockedLevel,
                        bestTurns = progress.bestTurns[level.id],
                    )
                },
            )
        }
    }
}

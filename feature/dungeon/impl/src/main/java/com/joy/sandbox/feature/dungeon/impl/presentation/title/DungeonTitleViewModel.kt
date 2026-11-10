package com.joy.sandbox.feature.dungeon.impl.presentation.title

import androidx.lifecycle.viewModelScope
import com.joy.sandbox.core.dungeon.model.DungeonLevels
import com.joy.sandbox.core.ui.BaseViewModel
import com.joy.sandbox.core.ui.CommandFlow
import com.joy.sandbox.feature.dungeon.impl.domain.DungeonProgressRepository
import com.joy.sandbox.feature.dungeon.impl.presentation.model.DungeonTitleUiModel
import com.joy.sandbox.feature.dungeon.impl.presentation.model.LevelItemUiModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/** Титул «Подземелья»: начало игры и список уровней с локами и рекордами. */
class DungeonTitleViewModel @Inject constructor(
    private val progressRepository: DungeonProgressRepository,
) : BaseViewModel() {

    val stateFlow = MutableStateFlow(DungeonTitleUiModel(levels = emptyList(), showLevels = false))

    val commandsFlow = CommandFlow<Commands>(viewModelScope)

    sealed interface Commands {

        data class OpenIntro(val levelId: Int) : Commands

        data object Exit : Commands
    }

    init {
        loadProgress()
    }

    fun onStartClick() {
        val levels = stateFlow.value.levels
        val next = levels.firstOrNull { !it.locked && it.bestTurns == null }?.id
            ?: levels.lastOrNull { !it.locked }?.id
            ?: return
        commandsFlow.tryEmit(Commands.OpenIntro(next))
    }

    fun onLevelsToggle() {
        stateFlow.value = stateFlow.value.copy(showLevels = !stateFlow.value.showLevels)
    }

    fun onLevelClick(levelId: Int) {
        val level = stateFlow.value.levels.firstOrNull { it.id == levelId } ?: return
        if (!level.locked) commandsFlow.tryEmit(Commands.OpenIntro(levelId))
    }

    fun onBackClick() {
        commandsFlow.tryEmit(Commands.Exit)
    }

    private fun loadProgress() {
        viewModelScope.launch {
            val progress = progressRepository.load()
            stateFlow.value = DungeonTitleUiModel(
                levels = DungeonLevels.ALL.map { level ->
                    LevelItemUiModel(
                        id = level.id,
                        name = level.name,
                        par = level.par,
                        locked = level.id > progress.unlockedLevel,
                        bestTurns = progress.bestTurns[level.id],
                    )
                },
                showLevels = stateFlow.value.showLevels,
            )
        }
    }
}

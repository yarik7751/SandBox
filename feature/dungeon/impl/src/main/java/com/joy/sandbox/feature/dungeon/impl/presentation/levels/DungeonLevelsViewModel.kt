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
 * Экран выбора уровня: лента с локами и рекордами. Лента растёт с прогрессом:
 * видны пройденные уровни и следующий (залоченный) — подгрузка по факту
 * прохождения, а не вся сгенерированная последовательность сразу.
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
            val progress = progressRepository.load()
            val maxVisibleId = progress.unlockedLevel + VISIBLE_AHEAD
            stateFlow.value = DungeonLevelsUiModel(
                levels = levelsInteractor.levels()
                    .filter { it.id <= maxVisibleId }
                    .map { level ->
                        LevelItemUiModel(
                            id = level.id,
                            name = level.name,
                            locked = level.id > progress.unlockedLevel,
                            bestTurns = progress.bestTurns[level.id],
                        )
                    },
            )
        }
    }

    private companion object {

        /** Сколько уровней вперёд видно сверх разблокированного: следующий залоченный. */
        const val VISIBLE_AHEAD = 1
    }
}

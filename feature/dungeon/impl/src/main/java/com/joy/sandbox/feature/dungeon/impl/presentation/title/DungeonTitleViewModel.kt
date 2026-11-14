package com.joy.sandbox.feature.dungeon.impl.presentation.title

import androidx.lifecycle.viewModelScope
import com.joy.sandbox.core.ui.BaseViewModel
import com.joy.sandbox.core.ui.CommandFlow
import com.joy.sandbox.feature.dungeon.impl.domain.DungeonProgressRepository
import javax.inject.Inject
import kotlinx.coroutines.launch

/** Титул «Подземелья»: «Начать» — последний открытый уровень, «Уровни» — экран выбора. */
class DungeonTitleViewModel @Inject constructor(
    private val progressRepository: DungeonProgressRepository,
) : BaseViewModel() {

    val commandsFlow = CommandFlow<Commands>(viewModelScope)

    sealed interface Commands {

        data class OpenGame(val levelId: Int) : Commands

        data object OpenLevels : Commands
    }

    fun onStartClick() {
        viewModelScope.launch {
            val progress = progressRepository.load()
            commandsFlow.tryEmit(Commands.OpenGame(progress.unlockedLevel))
        }
    }

    fun onLevelsClick() {
        commandsFlow.tryEmit(Commands.OpenLevels)
    }
}

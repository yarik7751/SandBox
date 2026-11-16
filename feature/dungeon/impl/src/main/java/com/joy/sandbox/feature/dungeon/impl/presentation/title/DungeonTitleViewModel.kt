package com.joy.sandbox.feature.dungeon.impl.presentation.title

import androidx.lifecycle.viewModelScope
import com.joy.sandbox.core.ui.BaseViewModel
import com.joy.sandbox.core.ui.CommandFlow
import com.joy.sandbox.feature.dungeon.impl.domain.DungeonIntroInteractor
import com.joy.sandbox.feature.dungeon.impl.domain.DungeonProgressRepository
import javax.inject.Inject
import kotlinx.coroutines.launch

/** Титул «Подземелья»: «Начать» — последний открытый уровень, «Уровни» — экран выбора. */
class DungeonTitleViewModel @Inject constructor(
    private val progressRepository: DungeonProgressRepository,
    private val introInteractor: DungeonIntroInteractor,
) : BaseViewModel() {

    val commandsFlow = CommandFlow<Commands>(viewModelScope)

    sealed interface Commands {

        data class OpenGame(val levelId: Int) : Commands

        data class OpenIntro(val levelId: Int) : Commands

        data object OpenLevels : Commands
    }

    fun onStartClick() {
        viewModelScope.launch {
            val levelId = progressRepository.load().unlockedLevel
            val command = if (introInteractor.introForLevel(levelId) != null) {
                Commands.OpenIntro(levelId)
            } else {
                Commands.OpenGame(levelId)
            }
            commandsFlow.tryEmit(command)
        }
    }

    fun onLevelsClick() {
        commandsFlow.tryEmit(Commands.OpenLevels)
    }
}

package com.joy.sandbox.feature.dungeon.impl.presentation.intro

import androidx.lifecycle.viewModelScope
import com.joy.sandbox.core.ui.BaseViewModel
import com.joy.sandbox.core.ui.CommandFlow
import com.joy.sandbox.feature.dungeon.impl.domain.DungeonIntroInteractor
import com.joy.sandbox.feature.dungeon.impl.presentation.model.DungeonIntroUiModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * Интро уровня: история уровня и новые механики — перед входом в подземелье.
 * Если непрочитанных механик нет, экран сам сразу уходит в игру.
 */
class DungeonIntroViewModel @Inject constructor(
    private val introInteractor: DungeonIntroInteractor,
) : BaseViewModel() {

    val stateFlow = MutableStateFlow<DungeonIntroUiModel?>(null)

    val commandsFlow = CommandFlow<Commands>(viewModelScope)

    sealed interface Commands {

        data class OpenGame(val levelId: Int) : Commands
    }

    fun init(levelId: Int) {
        if (stateFlow.value != null) return
        viewModelScope.launch {
            val intro = introInteractor.introForLevel(levelId)
            if (intro == null) {
                commandsFlow.tryEmit(Commands.OpenGame(levelId))
            } else {
                stateFlow.value = DungeonIntroUiModel(
                    levelId = intro.levelId,
                    levelName = intro.levelName,
                    intro = intro.levelIntro,
                    mechanics = intro.newMechanics,
                )
            }
        }
    }

    fun onEnterClick() {
        val state = stateFlow.value ?: return
        viewModelScope.launch {
            // Прочитал = дошёл до кнопки: фиксируем все механики экрана.
            introInteractor.markMechanicsSeen(state.mechanics)
            commandsFlow.tryEmit(Commands.OpenGame(state.levelId))
        }
    }
}

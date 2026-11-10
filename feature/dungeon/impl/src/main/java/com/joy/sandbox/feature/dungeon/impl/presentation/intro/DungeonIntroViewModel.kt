package com.joy.sandbox.feature.dungeon.impl.presentation.intro

import androidx.lifecycle.viewModelScope
import com.joy.sandbox.core.dungeon.model.DungeonLevels
import com.joy.sandbox.core.ui.BaseViewModel
import com.joy.sandbox.core.ui.CommandFlow
import com.joy.sandbox.feature.dungeon.impl.presentation.model.DungeonIntroUiModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow

/** Интро уровня: название, история, цель и пар. */
class DungeonIntroViewModel @Inject constructor() : BaseViewModel() {

    val stateFlow = MutableStateFlow<DungeonIntroUiModel?>(null)

    val commandsFlow = CommandFlow<Commands>(viewModelScope)

    sealed interface Commands {

        data class OpenGame(val levelId: Int) : Commands
    }

    fun init(levelId: Int) {
        if (stateFlow.value != null) return
        val level = DungeonLevels.byId(levelId)
        stateFlow.value = DungeonIntroUiModel(
            levelId = level.id,
            levelName = level.name,
            intro = level.intro,
            par = level.par,
        )
    }

    fun onEnterClick() {
        val levelId = stateFlow.value?.levelId ?: return
        commandsFlow.tryEmit(Commands.OpenGame(levelId))
    }
}

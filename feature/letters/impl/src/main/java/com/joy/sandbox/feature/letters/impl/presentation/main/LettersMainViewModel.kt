package com.joy.sandbox.feature.letters.impl.presentation.main

import androidx.lifecycle.viewModelScope
import com.joy.sandbox.core.ui.BaseViewModel
import com.joy.sandbox.core.ui.CommandFlow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

/**
 * Главный экран фичи «Буквы»: кнопки ведут в настройки дневного
 * и ночного режимов, у каждого — свой экран настроек.
 */
class LettersMainViewModel @Inject constructor() : BaseViewModel() {

    val state = MutableStateFlow(State())

    val commandsFlow = CommandFlow<Commands>(viewModelScope)

    data class State(
        val isLoading: Boolean = false,
    )

    sealed interface Commands {

        data object OpenDaySettings : Commands

        data object OpenNightSettings : Commands
    }

    fun onDayClick() {
        commandsFlow.tryEmit(Commands.OpenDaySettings)
    }

    fun onNightClick() {
        commandsFlow.tryEmit(Commands.OpenNightSettings)
    }
}

package com.joy.sandbox.feature.home.impl.presentation

import androidx.lifecycle.viewModelScope
import com.joy.sandbox.core.ui.BaseViewModel
import com.joy.sandbox.core.ui.CommandFlow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class HomeViewModel @Inject constructor() : BaseViewModel() {

    val state = MutableStateFlow(State())

    val commandsFlow = CommandFlow<Commands>(viewModelScope)

    data class State(
        val isLoading: Boolean = false,
    )

    sealed interface Commands {

        data object OpenWebSocket : Commands
    }

    fun onWebSocketClick() {
        commandsFlow.tryEmit(Commands.OpenWebSocket)
    }
}

package com.joy.sandbox.feature.start.impl.presentation

import androidx.lifecycle.viewModelScope
import com.joy.sandbox.core.ui.BaseViewModel
import com.joy.sandbox.core.ui.CommandFlow
import com.joy.sandbox.core.ui.emit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

/**
 * Экран инициализации приложения: здесь инициализируются фичи и настройки,
 * после чего команда [Commands.OpenHome] заменяет стартовый экран на Home.
 */
class StartViewModel @Inject constructor() : BaseViewModel() {

    val stateFlow = MutableStateFlow(State())
    val commandsFlow = CommandFlow<Commands>(viewModelScope)
    private val isInitializedFlow = MutableStateFlow(false)

    init {
        isInitializedFlow.onEach { isInitialized ->
            if (isInitialized) {
                openHome()
            }
        }.launchIn(viewModelScope)
    }

    private fun openHome() {
        commandsFlow emit Commands.OpenHome
    }

    /**
     * Вызывается экраном, когда инициализация фичей и настроек завершена.
     */
    fun onInitialized() {
        isInitializedFlow.value = true
    }

    data class State(
        val isLoading: Boolean = false,
    )

    sealed interface Commands {
        data object OpenHome : Commands
    }
}

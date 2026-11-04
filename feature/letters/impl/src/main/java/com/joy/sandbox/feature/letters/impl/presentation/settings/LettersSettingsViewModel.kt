package com.joy.sandbox.feature.letters.impl.presentation.settings

import androidx.lifecycle.viewModelScope
import com.joy.sandbox.core.ui.BaseViewModel
import com.joy.sandbox.core.ui.CommandFlow
import com.joy.sandbox.feature.letters.api.model.Alphabet
import com.joy.sandbox.feature.letters.api.model.LettersOrder
import com.joy.sandbox.feature.letters.impl.domain.LettersInteractor
import com.joy.sandbox.feature.letters.impl.presentation.settings.mapper.SettingsUiMapper
import com.joy.sandbox.feature.letters.impl.presentation.settings.model.SettingsOptionUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class LettersSettingsViewModel @Inject constructor(
    private val lettersInteractor: LettersInteractor,
    private val settingsUiMapper: SettingsUiMapper,
) : BaseViewModel() {

    val stateFlow = MutableStateFlow(State())

    val commandsFlow = CommandFlow<Commands>(viewModelScope)

    private var selectedAlphabet: Alphabet = lettersInteractor.getSelectedAlphabet()
    private var selectedOrder: LettersOrder = lettersInteractor.getSelectedOrder()

    data class State(
        val alphabetOptions: List<SettingsOptionUiModel<Alphabet>> = emptyList(),
        val orderOptions: List<SettingsOptionUiModel<LettersOrder>> = emptyList(),
    )

    sealed interface Commands {

        data class Run(
            val alphabet: Alphabet,
            val order: LettersOrder,
        ) : Commands
    }

    init {
        updateOptions()
    }

    fun onAlphabetSelect(alphabet: Alphabet) {
        lettersInteractor.saveAlphabet(alphabet)
        selectedAlphabet = alphabet
        updateOptions()
    }

    fun onOrderSelect(order: LettersOrder) {
        lettersInteractor.saveOrder(order)
        selectedOrder = order
        updateOptions()
    }

    fun onRunClick() {
        commandsFlow.tryEmit(Commands.Run(selectedAlphabet, selectedOrder))
    }

    private fun updateOptions() {
        stateFlow.value = State(
            alphabetOptions = settingsUiMapper.mapAlphabetOptions(selectedAlphabet),
            orderOptions = settingsUiMapper.mapOrderOptions(selectedOrder),
        )
    }
}

package com.joy.sandbox.feature.letters.impl.presentation.settings.day

import androidx.lifecycle.viewModelScope
import com.joy.sandbox.core.ui.BaseViewModel
import com.joy.sandbox.core.ui.CommandFlow
import com.joy.sandbox.feature.letters.api.model.Alphabet
import com.joy.sandbox.feature.letters.api.model.LettersOrder
import com.joy.sandbox.feature.letters.impl.domain.LettersDaySettingsInteractor
import com.joy.sandbox.feature.letters.impl.presentation.settings.mapper.SettingsUiMapper
import com.joy.sandbox.feature.letters.impl.presentation.settings.model.SettingsOptionUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

/**
 * Дневные настройки фичи «Буквы»: алфавит, порядок показа и звук.
 * Настройки хранятся через [LettersDaySettingsInteractor].
 */
class LettersDaySettingsViewModel @Inject constructor(
    private val daySettingsInteractor: LettersDaySettingsInteractor,
    private val settingsUiMapper: SettingsUiMapper,
) : BaseViewModel() {

    val stateFlow = MutableStateFlow(State())

    val commandsFlow = CommandFlow<Commands>(viewModelScope)

    private var selectedAlphabet: Alphabet = daySettingsInteractor.getSelectedAlphabet()
    private var selectedOrder: LettersOrder = daySettingsInteractor.getSelectedOrder()
    private var soundEnabled: Boolean = daySettingsInteractor.isSoundEnabled()

    data class State(
        val alphabetOptions: List<SettingsOptionUiModel<Alphabet>> = emptyList(),
        val orderOptions: List<SettingsOptionUiModel<LettersOrder>> = emptyList(),
        val soundEnabled: Boolean = true,
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
        daySettingsInteractor.saveAlphabet(alphabet)
        selectedAlphabet = alphabet
        updateOptions()
    }

    fun onOrderSelect(order: LettersOrder) {
        daySettingsInteractor.saveOrder(order)
        selectedOrder = order
        updateOptions()
    }

    fun onSoundToggle(enabled: Boolean) {
        daySettingsInteractor.saveSoundEnabled(enabled)
        soundEnabled = enabled
        updateOptions()
    }

    fun onRunClick() {
        commandsFlow.tryEmit(Commands.Run(selectedAlphabet, selectedOrder))
    }

    private fun updateOptions() {
        stateFlow.value = stateFlow.value.copy(
            alphabetOptions = settingsUiMapper.mapAlphabetOptions(selectedAlphabet),
            orderOptions = settingsUiMapper.mapOrderOptions(selectedOrder),
            soundEnabled = soundEnabled,
        )
    }
}

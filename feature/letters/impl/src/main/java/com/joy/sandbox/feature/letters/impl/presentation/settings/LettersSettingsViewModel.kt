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
    private var soundEnabled: Boolean = lettersInteractor.isSoundEnabled()
    private var selectedNightMinutes: Int = lettersInteractor.getNightDurationMin()

    data class State(
        val alphabetOptions: List<SettingsOptionUiModel<Alphabet>> = emptyList(),
        val orderOptions: List<SettingsOptionUiModel<LettersOrder>> = emptyList(),
        val soundEnabled: Boolean = true,
        val nightDurationOptions: List<SettingsOptionUiModel<Int>> = emptyList(),
    )

    sealed interface Commands {

        data class Run(
            val alphabet: Alphabet,
            val order: LettersOrder,
        ) : Commands

        data class RunNight(
            val alphabet: Alphabet,
            val order: LettersOrder,
            val minutes: Int,
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

    fun onSoundToggle(enabled: Boolean) {
        lettersInteractor.saveSoundEnabled(enabled)
        soundEnabled = enabled
        updateOptions()
    }

    fun onNightDurationSelect(minutes: Int) {
        lettersInteractor.saveNightDurationMin(minutes)
        selectedNightMinutes = minutes
        updateOptions()
    }

    fun onRunClick() {
        commandsFlow.tryEmit(Commands.Run(selectedAlphabet, selectedOrder))
    }

    fun onNightRunClick() {
        commandsFlow.tryEmit(Commands.RunNight(selectedAlphabet, selectedOrder, selectedNightMinutes))
    }

    private fun updateOptions() {
        stateFlow.value = State(
            alphabetOptions = settingsUiMapper.mapAlphabetOptions(selectedAlphabet),
            orderOptions = settingsUiMapper.mapOrderOptions(selectedOrder),
            soundEnabled = soundEnabled,
            nightDurationOptions = settingsUiMapper.mapNightDurationOptions(selectedNightMinutes),
        )
    }
}

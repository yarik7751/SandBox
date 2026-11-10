package com.joy.sandbox.feature.letters.impl.presentation.settings.night

import androidx.lifecycle.viewModelScope
import com.joy.sandbox.core.ui.BaseViewModel
import com.joy.sandbox.core.ui.CommandFlow
import com.joy.sandbox.feature.letters.api.model.Alphabet
import com.joy.sandbox.feature.letters.api.model.LettersOrder
import com.joy.sandbox.feature.letters.impl.domain.LettersNightSettingsInteractor
import com.joy.sandbox.feature.letters.impl.presentation.settings.mapper.SettingsUiMapper
import com.joy.sandbox.feature.letters.impl.presentation.settings.model.SettingsOptionUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

/**
 * Ночные настройки фичи «Буквы»: алфавит, порядок показа и длительность
 * таймера. Настройки хранятся через [LettersNightSettingsInteractor].
 */
class LettersNightSettingsViewModel @Inject constructor(
    private val nightSettingsInteractor: LettersNightSettingsInteractor,
    private val settingsUiMapper: SettingsUiMapper,
) : BaseViewModel() {

    val stateFlow = MutableStateFlow(State())

    val commandsFlow = CommandFlow<Commands>(viewModelScope)

    private var selectedAlphabet: Alphabet = nightSettingsInteractor.getSelectedAlphabet()
    private var selectedOrder: LettersOrder = nightSettingsInteractor.getSelectedOrder()
    private var selectedMinutes: Int = nightSettingsInteractor.getNightDurationMin()

    data class State(
        val alphabetOptions: List<SettingsOptionUiModel<Alphabet>> = emptyList(),
        val orderOptions: List<SettingsOptionUiModel<LettersOrder>> = emptyList(),
        val durationOptions: List<SettingsOptionUiModel<Int>> = emptyList(),
    )

    sealed interface Commands {

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
        nightSettingsInteractor.saveAlphabet(alphabet)
        selectedAlphabet = alphabet
        updateOptions()
    }

    fun onOrderSelect(order: LettersOrder) {
        nightSettingsInteractor.saveOrder(order)
        selectedOrder = order
        updateOptions()
    }

    fun onDurationSelect(minutes: Int) {
        nightSettingsInteractor.saveNightDurationMin(minutes)
        selectedMinutes = minutes
        updateOptions()
    }

    fun onRunClick() {
        commandsFlow.tryEmit(
            Commands.RunNight(selectedAlphabet, selectedOrder, selectedMinutes),
        )
    }

    private fun updateOptions() {
        stateFlow.value = stateFlow.value.copy(
            alphabetOptions = settingsUiMapper.mapAlphabetOptions(selectedAlphabet),
            orderOptions = settingsUiMapper.mapOrderOptions(selectedOrder),
            durationOptions = settingsUiMapper.mapNightDurationOptions(selectedMinutes),
        )
    }
}

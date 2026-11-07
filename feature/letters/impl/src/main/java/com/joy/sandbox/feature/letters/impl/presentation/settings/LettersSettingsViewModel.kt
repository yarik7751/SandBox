package com.joy.sandbox.feature.letters.impl.presentation.settings

import androidx.lifecycle.viewModelScope
import com.joy.sandbox.core.ui.BaseViewModel
import com.joy.sandbox.core.ui.CommandFlow
import com.joy.sandbox.feature.letters.api.model.Alphabet
import com.joy.sandbox.feature.letters.api.model.LettersOrder
import com.joy.sandbox.feature.letters.impl.domain.LettersDaySettingsInteractor
import com.joy.sandbox.feature.letters.impl.domain.LettersMode
import com.joy.sandbox.feature.letters.impl.domain.LettersModeInteractor
import com.joy.sandbox.feature.letters.impl.domain.LettersNightSettingsInteractor
import com.joy.sandbox.feature.letters.impl.presentation.settings.mapper.SettingsUiMapper
import com.joy.sandbox.feature.letters.impl.presentation.settings.model.SettingsOptionUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

/**
 * Настройки фичи «Буквы». Экран разбит на две вкладки-режима
 * ([LettersMode]): «День» и «Ночь»; у каждой режима свои настройки
 * (включая алфавит и порядок показа) и свой источник данных —
 * дневные через [LettersDaySettingsInteractor], ночные через
 * [LettersNightSettingsInteractor].
 */
class LettersSettingsViewModel @Inject constructor(
    private val daySettingsInteractor: LettersDaySettingsInteractor,
    private val nightSettingsInteractor: LettersNightSettingsInteractor,
    private val lettersModeInteractor: LettersModeInteractor,
    private val settingsUiMapper: SettingsUiMapper,
) : BaseViewModel() {

    // При входе на экран открывается последний выбранный режим;
    // если режим ещё не выбирался — ночной (см. LettersModeRepository).
    val stateFlow = MutableStateFlow(
        State(selectedMode = lettersModeInteractor.loadLastMode()),
    )

    val commandsFlow = CommandFlow<Commands>(viewModelScope)

    private var selectedAlphabet: Alphabet = daySettingsInteractor.getSelectedAlphabet()
    private var selectedOrder: LettersOrder = daySettingsInteractor.getSelectedOrder()
    private var soundEnabled: Boolean = daySettingsInteractor.isSoundEnabled()

    private var selectedNightAlphabet: Alphabet = nightSettingsInteractor.getSelectedAlphabet()
    private var selectedNightOrder: LettersOrder = nightSettingsInteractor.getSelectedOrder()
    private var selectedNightMinutes: Int = nightSettingsInteractor.getNightDurationMin()

    data class State(
        val selectedMode: LettersMode = LettersMode.DAY,
        val alphabetOptions: List<SettingsOptionUiModel<Alphabet>> = emptyList(),
        val orderOptions: List<SettingsOptionUiModel<LettersOrder>> = emptyList(),
        val soundEnabled: Boolean = true,
        val nightAlphabetOptions: List<SettingsOptionUiModel<Alphabet>> = emptyList(),
        val nightOrderOptions: List<SettingsOptionUiModel<LettersOrder>> = emptyList(),
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

    fun onModeSelect(mode: LettersMode) {
        lettersModeInteractor.saveMode(mode)
        stateFlow.value = stateFlow.value.copy(selectedMode = mode)
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

    fun onNightAlphabetSelect(alphabet: Alphabet) {
        nightSettingsInteractor.saveAlphabet(alphabet)
        selectedNightAlphabet = alphabet
        updateOptions()
    }

    fun onNightOrderSelect(order: LettersOrder) {
        nightSettingsInteractor.saveOrder(order)
        selectedNightOrder = order
        updateOptions()
    }

    fun onNightDurationSelect(minutes: Int) {
        nightSettingsInteractor.saveNightDurationMin(minutes)
        selectedNightMinutes = minutes
        updateOptions()
    }

    fun onRunClick() {
        commandsFlow.tryEmit(Commands.Run(selectedAlphabet, selectedOrder))
    }

    fun onNightRunClick() {
        commandsFlow.tryEmit(
            Commands.RunNight(selectedNightAlphabet, selectedNightOrder, selectedNightMinutes),
        )
    }

    private fun updateOptions() {
        stateFlow.value = stateFlow.value.copy(
            alphabetOptions = settingsUiMapper.mapAlphabetOptions(selectedAlphabet),
            orderOptions = settingsUiMapper.mapOrderOptions(selectedOrder),
            soundEnabled = soundEnabled,
            nightAlphabetOptions = settingsUiMapper.mapAlphabetOptions(selectedNightAlphabet),
            nightOrderOptions = settingsUiMapper.mapOrderOptions(selectedNightOrder),
            nightDurationOptions = settingsUiMapper.mapNightDurationOptions(selectedNightMinutes),
        )
    }
}

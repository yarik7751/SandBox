package com.joy.sandbox.feature.letters.impl.presentation.letters

import androidx.lifecycle.viewModelScope
import com.joy.sandbox.core.ui.BaseViewModel
import com.joy.sandbox.core.ui.CommandFlow
import com.joy.sandbox.feature.letters.api.model.Alphabet
import com.joy.sandbox.feature.letters.api.model.LettersOrder
import com.joy.sandbox.feature.letters.impl.domain.LettersInteractor
import com.joy.sandbox.feature.letters.impl.domain.LettersSpeaker
import com.joy.sandbox.feature.letters.impl.presentation.letters.LetterColorProvider
import com.joy.sandbox.feature.letters.impl.presentation.letters.mapper.LetterUiModelMapper
import com.joy.sandbox.feature.letters.impl.presentation.letters.model.FillDirection
import com.joy.sandbox.feature.letters.impl.presentation.letters.model.LetterUiModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Показывает буквы по одной: контур уже на месте, заливка растёт —
 * как будто букву закрашивают вручную. Сама заливка кадрируется
 * на экране (см. LettersScreen), ViewModel только отмеряет её
 * длительность [FILL_DURATION_MS].
 * После полной заливки озвучивается название буквы; следующая буква
 * появляется только по окончании проигрывания.
 *
 * Экран без обработки нажатий: единственное действие — жест-крест,
 * который приводит к [Commands.ExitToHome].
 */
class LettersViewModel @Inject constructor(
    private val lettersInteractor: LettersInteractor,
    private val letterUiModelMapper: LetterUiModelMapper,
    private val letterColorProvider: LetterColorProvider,
    private val lettersSpeaker: LettersSpeaker,
) : BaseViewModel() {

    val stateFlow = MutableStateFlow(State())

    val commandsFlow = CommandFlow<Commands>(viewModelScope)

    private var initialized = false
    private var showJob: Job? = null
    private var alphabet: Alphabet = Alphabet.CYRILLIC
    private var soundEnabled: Boolean = true

    data class State(
        val letter: LetterUiModel? = null,
        // false по умолчанию: первая буква тоже появляется через fade.
        val letterVisible: Boolean = false,
        /** true — экран анимирует заливку буквы (0 → 1 за [FILL_DURATION_MS]). */
        val fillActive: Boolean = false,
    )

    sealed interface Commands {

        data object ExitToHome : Commands
    }

    /**
     * Запускает показ. Вызывается из экрана один раз; повторные вызовы
     * (восстановление после пересоздания) игнорируются.
     */
    fun init(alphabet: Alphabet, order: LettersOrder) {
        if (initialized) return
        initialized = true
        this.alphabet = alphabet
        soundEnabled = lettersInteractor.isSoundEnabled()
        val letters = lettersInteractor.getLetters(alphabet, order)
        startShow(letters)
    }

    fun onCrossDrawn() {
        commandsFlow.tryEmit(Commands.ExitToHome)
    }

    override fun onCleared() {
        showJob?.cancel()
        lettersSpeaker.release()
        super.onCleared()
    }

    private fun startShow(letters: List<Char>) {
        val colors = letterColorProvider.shuffledPalette()
        showJob = viewModelScope.launch {
            var index = 0
            while (isActive) {
                val letter = letterUiModelMapper.map(
                    char = letters[index],
                    fillColor = colors[index % colors.size],
                    fillDirection = FillDirection.entries.random(),
                )
                // Новая буква появляется через fade (letterVisible = true),
                // экран стартует её заливку (fillActive = true).
                stateFlow.value = State(
                    letter = letter,
                    letterVisible = true,
                    fillActive = true,
                )

                // Заливка идёт FILL_DURATION_MS; ожидание совпадает
                // с ней по длительности, озвучка — только после неё.
                delay(FILL_DURATION_MS)

                // Буква полностью закрашена: озвучиваем название и ждём
                // конца проигрывания — только потом уходим на следующую.
                // Звук выключен — проигрывания нет, буква сменяется сразу.
                if (soundEnabled) {
                    lettersSpeaker.awaitLetterSpoken(letters[index], alphabet)
                }

                delay(AFTER_SPEECH_HOLD_MS)

                // Исчезновение через fade перед переключением на следующую.
                stateFlow.update { it.copy(letterVisible = false) }
                delay(FADE_DURATION_MS)

                index = (index + 1) % letters.size
            }
        }
    }

    internal companion object {

        const val FILL_DURATION_MS = 4_000L
        const val AFTER_SPEECH_HOLD_MS = 500L
        const val FADE_DURATION_MS = 800L
    }
}

package com.joy.sandbox.feature.letters.impl.presentation.letters

import androidx.lifecycle.viewModelScope
import com.joy.sandbox.core.ui.BaseViewModel
import com.joy.sandbox.core.ui.CommandFlow
import com.joy.sandbox.feature.letters.api.model.Alphabet
import com.joy.sandbox.feature.letters.api.model.LettersOrder
import com.joy.sandbox.feature.letters.impl.domain.LettersInteractor
import com.joy.sandbox.feature.letters.impl.presentation.letters.mapper.LetterUiModelMapper
import com.joy.sandbox.feature.letters.impl.presentation.letters.model.LetterUiModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Показывает буквы по одной: контур уже на месте, заливка растёт
 * ([State.fillProgress]) — как будто букву закрашивают вручную.
 *
 * Экран без обработки нажатий: единственное действие — жест-крест,
 * который приводит к [Commands.ExitToHome].
 */
class LettersViewModel @Inject constructor(
    private val lettersInteractor: LettersInteractor,
    private val letterUiModelMapper: LetterUiModelMapper,
) : BaseViewModel() {

    val stateFlow = MutableStateFlow(State())

    val commandsFlow = CommandFlow<Commands>(viewModelScope)

    private var initialized = false
    private var showJob: Job? = null

    data class State(
        val letter: LetterUiModel? = null,
        val fillProgress: Float = 0f,
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
        val letters = lettersInteractor.getLetters(alphabet, order)
        startShow(letters)
    }

    fun onCrossDrawn() {
        commandsFlow.tryEmit(Commands.ExitToHome)
    }

    override fun onCleared() {
        showJob?.cancel()
        super.onCleared()
    }

    private fun startShow(letters: List<Char>) {
        showJob = viewModelScope.launch {
            var index = 0
            while (isActive) {
                val letter = letterUiModelMapper.map(letters[index])
                stateFlow.value = State(letter = letter, fillProgress = 0f)

                var elapsed = 0L
                while (elapsed < FILL_DURATION_MS && isActive) {
                    delay(TICK_MS)
                    elapsed += TICK_MS
                    val progress = (elapsed.toFloat() / FILL_DURATION_MS).coerceIn(0f, 1f)
                    stateFlow.update { it.copy(fillProgress = progress) }
                }

                delay(LETTER_HOLD_MS)

                index = (index + 1) % letters.size
            }
        }
    }

    private companion object {

        const val FILL_DURATION_MS = 4_000L
        const val LETTER_HOLD_MS = 1_500L
        const val TICK_MS = 50L
    }
}

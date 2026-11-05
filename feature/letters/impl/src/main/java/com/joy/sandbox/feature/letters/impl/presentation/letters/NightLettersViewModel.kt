package com.joy.sandbox.feature.letters.impl.presentation.letters

import androidx.lifecycle.viewModelScope
import androidx.compose.ui.graphics.Color
import com.joy.sandbox.core.ui.BaseViewModel
import com.joy.sandbox.core.ui.CommandFlow
import com.joy.sandbox.feature.letters.api.model.Alphabet
import com.joy.sandbox.feature.letters.api.model.LettersOrder
import com.joy.sandbox.feature.letters.impl.domain.LettersDaySettingsInteractor
import com.joy.sandbox.feature.letters.impl.presentation.letters.mapper.LetterUiModelMapper
import com.joy.sandbox.feature.letters.impl.presentation.letters.model.FillDirection
import com.joy.sandbox.feature.letters.impl.presentation.letters.model.LetterUiModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Ночной режим показа букв для засыпания (см. [NightModeConfig]):
 * диктор отключен, буква показывается сразу залитой в тёплом тусклом
 * цвете, темп линейно замедляется по ходу сеанса, в последнюю минуту
 * таймера яркость плавно сползает к минимуму, после чего кадры ещё
 * немного сменяются и экран затухает к чёрному.
 *
 * Пауза/возобновление (сворачивание приложения) — [pause]/[resume]:
 * корутина отменяется, позиция и прошедшее время сохраняются, при
 * возврате показ продолжается с того же кадра.
 */
class NightLettersViewModel @Inject constructor(
    private val lettersInteractor: LettersDaySettingsInteractor,
    private val letterUiModelMapper: LetterUiModelMapper,
) : BaseViewModel() {

    val stateFlow = MutableStateFlow(State())

    val commandsFlow = CommandFlow<Commands>(viewModelScope)

    private var initialized = false
    private var showJob: Job? = null
    private var paused = false

    private var letters: List<Char> = emptyList()
    private var colors: List<Color> = emptyList()
    private var index = 0

    /** Длительность сеанса; null — бесконечный режим. */
    private var durationMs: Long? = null

    /** Прошедшее время сеанса (нарастает тиками, переживает паузу). */
    private var elapsedSessionMs = 0L

    /** Таймер закончился, идёт финальное удержание кадров. */
    private var timerEnded = false
    private var holdElapsedMs = 0L

    data class State(
        val letter: LetterUiModel? = null,
        // false по умолчанию: первая буква тоже появляется через fade.
        val letterVisible: Boolean = false,
        /** Яркость окна (0.15 → 0.05 к концу таймера). */
        val brightness: Float = NightModeConfig.START_BRIGHTNESS,
        /** Сеанс завершён: экран уходит к чисто чёрному. */
        val finished: Boolean = false,
    )

    sealed interface Commands {

        data object ExitToHome : Commands
    }

    /**
     * Запускает сеанс. Вызывается из экрана один раз; повторные вызовы
     * (восстановление после пересоздания) игнорируются.
     */
    fun init(alphabet: Alphabet, order: LettersOrder, minutes: Int) {
        if (initialized) return
        initialized = true
        // Алфавит и порядок — свои, ночные (переданы route-аргументами
        // из ночного раздела настроек); звук игнорируется — диктор
        // в ночном режиме не используется вовсе.
        letters = lettersInteractor.getLetters(alphabet, order)
        colors = NightModeConfig.WARM_PALETTE.shuffled()
        durationMs = if (minutes == NightModeConfig.INFINITE_MINUTES) {
            null
        } else {
            minutes.toLong() * 60_000L
        }
        startShow()
    }

    /** Пауза при сворачивании приложения (onStop). */
    fun pause() {
        if (!initialized || paused || showJob == null) return
        paused = true
        showJob?.cancel()
        showJob = null
    }

    /** Продолжение с того же кадра при возврате (onStart). */
    fun resume() {
        if (!initialized || !paused) return
        paused = false
        startShow()
    }

    fun onCrossDrawn() {
        commandsFlow.tryEmit(Commands.ExitToHome)
    }

    override fun onCleared() {
        showJob?.cancel()
        super.onCleared()
    }

    private fun startShow() {
        showJob = viewModelScope.launch {
            while (isActive) {
                showCurrentLetter()

                // Удержание кадра; тиками обновляем elapsed и яркость
                // и ловим момент окончания таймера.
                val interval = if (timerEnded) {
                    NightModeConfig.MAX_FRAME_MS
                } else {
                    NightModeConfig.frameIntervalMs(tempoProgress())
                }
                var held = 0L
                while (held < interval && isActive) {
                    delay(NightModeConfig.TICK_MS)
                    held += NightModeConfig.TICK_MS
                    elapsedSessionMs += NightModeConfig.TICK_MS
                    updateBrightness()
                    if (!timerEnded && isTimerFinished()) {
                        enterHoldPhase()
                        break
                    }
                }
                if (!isActive) return@launch

                if (timerEnded) {
                    holdElapsedMs += interval
                    if (holdElapsedMs >= NightModeConfig.FINAL_HOLD_MS) {
                        finishSession()
                        return@launch
                    }
                }

                // Исчезновение через fade — чёрный промежуток между кадрами.
                stateFlow.update { it.copy(letterVisible = false) }
                delay(NightModeConfig.FRAME_FADE_MS)
                if (!isActive) return@launch

                index = (index + 1) % letters.size
            }
        }
    }

    private fun showCurrentLetter() {
        val letter = letterUiModelMapper.map(
            char = letters[index],
            // Как в дневном режиме: палитра перемешивается на старте
            // сеанса, цвета берутся по кругу — за сеанс все цвета
            // показываются примерно одинаковое число раз.
            fillColor = colors[index % colors.size],
            fillDirection = FillDirection.entries.random(),
        )
        stateFlow.value = State(
            letter = letter,
            letterVisible = true,
            brightness = stateFlow.value.brightness,
            finished = false,
        )
    }

    /** Прогресс замедления темпа: доля сеанса (0..1). */
    private fun tempoProgress(): Float {
        val horizon = durationMs ?: NightModeConfig.INFINITE_PROGRESS_HORIZON_MS
        return (elapsedSessionMs.toFloat() / horizon).coerceIn(0f, 1f)
    }

    private fun isTimerFinished(): Boolean {
        val duration = durationMs ?: return false
        return elapsedSessionMs >= duration
    }

    private fun enterHoldPhase() {
        timerEnded = true
        stateFlow.update { it.copy(brightness = NightModeConfig.MIN_BRIGHTNESS) }
    }

    /** 2.4: плавное сползание яркости к минимуму в последние 60 с. */
    private fun updateBrightness() {
        val duration = durationMs ?: return
        val remaining = duration - elapsedSessionMs
        val brightness = when {
            remaining <= 0L -> NightModeConfig.MIN_BRIGHTNESS
            remaining >= NightModeConfig.DIM_DURATION_MS -> NightModeConfig.START_BRIGHTNESS
            else -> {
                val p = 1f - remaining.toFloat() / NightModeConfig.DIM_DURATION_MS
                NightModeConfig.START_BRIGHTNESS +
                    (NightModeConfig.MIN_BRIGHTNESS - NightModeConfig.START_BRIGHTNESS) * p
            }
        }
        stateFlow.update { if (it.brightness != brightness) it.copy(brightness = brightness) else it }
    }

    private suspend fun finishSession() {
        stateFlow.update { it.copy(letterVisible = false, finished = true) }
        delay(NightModeConfig.FINAL_BLACK_MS)
        if (currentCoroutineContext().isActive) commandsFlow.tryEmit(Commands.ExitToHome)
    }
}

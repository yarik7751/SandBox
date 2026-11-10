package com.joy.sandbox.feature.letters.impl.presentation.letters

import androidx.compose.ui.graphics.Color

/**
 * Конфигурация ночного режима показа букв — все параметры режима
 * (длительность, темп, палитра, яркость) в одном месте.
 *
 * Темп показа (п. 2.2 ТЗ): линейная прогрессия — интервал кадра растёт
 * от [BASE_FRAME_MS] к [MAX_FRAME_MS] по мере сеанса. Для конечного
 * таймера прогресс = доля прошедшего времени; для бесконечного сеанса
 * прогресс идёт к рубежу [INFINITE_PROGRESS_HORIZON_MS], дальше темп
 * остаётся на плато [MAX_FRAME_MS].
 */
object NightModeConfig {

    /** Базовый интервал кадра в начале сеанса. */
    const val BASE_FRAME_MS = 12_000L

    /** Интервал кадра к концу таймера. */
    const val MAX_FRAME_MS = 20_000L

    /** Fade между кадрами (исчезновение перед сменой буквы). */
    const val FRAME_FADE_MS = 1_750L

    /** Мягкий fade-in буквы при появлении. */
    const val LETTER_FADE_IN_MS = 1_000L

    /**
     * Волнистая заливка в ночном режиме включена, но в 2 раза
     * медленнее дневной (8 с против 4 с): медленная заливка
     * не будит, а мягко усыпляет.
     */
    const val FILL_DURATION_MS = 8_000L

    /** Яркость окна на старте сеанса (системные права не нужны). */
    const val START_BRIGHTNESS = 0.15f

    /** Яркость, к которой сползает экран в последнюю минуту таймера. */
    const val MIN_BRIGHTNESS = 0.05f

    /** Длительность сползания яркости перед концом таймера. */
    const val DIM_DURATION_MS = 60_000L

    /** Финальный fade к чисто чёрному экрану перед завершением. */
    const val FINAL_BLACK_MS = 2_000L

    /**
     * После окончания таймера кадры ещё сменяются это время на минимальной
     * яркости, затем — fade к чёрному и завершение (п. 2.4 ТЗ).
     */
    const val FINAL_HOLD_MS = 30_000L

    /**
     * Рубеж прогрессии темпа для бесконечного сеанса: к этому моменту
     * интервал кадра достигает [MAX_FRAME_MS] и дальше не растёт.
     */
    const val INFINITE_PROGRESS_HORIZON_MS = 30 * 60_000L

    /** Тик цикла показа и таймера. */
    const val TICK_MS = 100L

    /** Доступные значения таймера в минутах; -1 — бесконечность. */
    val TIMER_OPTIONS_MIN = listOf(10, 20, 30, INFINITE_MINUTES)

    const val INFINITE_MINUTES = -1

    /** Длительность таймера по умолчанию (минуты). */
    const val DEFAULT_MINUTES = 20

    /**
     * Фиксированный набор приглушённых тёплых тонов (низкая доля
     * синего компонента) — цвет выбирается случайно на каждый кадр.
     */
    val WARM_PALETTE = listOf(
        Color(0xFF8B3A2F),
        Color(0xFFA0522D),
        Color(0xFFB8703F),
        Color(0xFF7B4B3A),
        Color(0xFF964B33),
        Color(0xFF6E3B2A),
    )

    /** Тускло-серый контур буквы — не режет глаза в темноте. */
    val CONTOUR_COLOR: Color = Color(0xFF777777)

    /**
     * Интервал кадра для текущего момента сеанса: линейный рост
     * от [BASE_FRAME_MS] к [MAX_FRAME_MS] по мере [progress] (0..1).
     */
    fun frameIntervalMs(progress: Float): Long {
        val p = progress.coerceIn(0f, 1f)
        return BASE_FRAME_MS + ((MAX_FRAME_MS - BASE_FRAME_MS) * p).toLong()
    }
}

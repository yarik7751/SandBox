package com.joy.sandbox.feature.notifications.impl.domain

/**
 * Live Update — Android-аналог iOS Live Activity: ongoing progress-style
 * уведомление (Android 16), продвигаемое на экран блокировки и в чип
 * статус-бара (Android 16 QPR1+).
 *
 * В реальном приложении этапы прилетают с сервера по push (start/update/end).
 * Здесь — локальный таймер, цель та же: показать жизненный цикл.
 */
interface LiveUpdateInteractor {

    /** Показать Live Update на этапе [stage] (повторный вызов = обновление). */
    fun show(stage: LiveUpdateStage)

    /** Отменить Live Update (пользователь нажал кнопку ещё раз). */
    fun cancel()

    /** true, если пользователь сам убрал уведомление (свайп/открепление). */
    fun consumeUserDismissal(): Boolean

    /** Состояние возможности продвижения в Live Update на этом устройстве. */
    fun promotionStatus(): PromotionStatus
}

enum class PromotionStatus {

    /** API < 36: покажем обычное ongoing-уведомление с прогрессом. */
    UNSUPPORTED,

    /** API 36/36.1, но продвижение запрещено (настройка/пользователь/прошивка). */
    NOT_ALLOWED,

    /** Продвижение доступно — возможен чип в статус-баре и слот на lock screen. */
    AVAILABLE,
}

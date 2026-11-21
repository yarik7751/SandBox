package com.joy.sandbox.feature.notifications.impl.domain

/**
 * Точка входа фичи: всё, что умеет делать фича с уведомлениями.
 * UI (ViewModel) не знает про NotificationManager — только про эти методы.
 *
 * Методов много намеренно: это тренажёр, один метод = один вид уведомления.
 */
@Suppress("TooManyFunctions")
interface NotificationsInteractor {

    /** Создаёт базовые каналы (idempotентно) и проверяет актуальное состояние разрешения. */
    fun onScreenOpened()

    /** granted = POST_NOTIFICATIONS выдано (на API < 33 всегда true). */
    fun isPermissionGranted(): Boolean

    // ---- Каналы ----

    /** Создаёт новый канал «из кода» и шлёт в него уведомление. */
    fun createDynamicChannelAndNotify()

    /** Открывает системные настройки канала. */
    fun openChannelSettings()

    // ---- Базовые виды ----

    /** Базовое уведомление: иконка, заголовок, текст, бейдж, tap-action. */
    fun showBasic()

    /** Heads-up на канале HIGH — выпадает поверх экрана со звуком. */
    fun showHeadsUp()

    /** Тихое уведомление на канале LOW. */
    fun showLowPriority()

    // ---- Стиля раскрытия ----

    /** BigTextStyle — длинный текст, раскрывается свайпом вниз. */
    fun showBigText()

    /** BigPictureStyle — большая картинка (bitmap генерируется кодом). */
    fun showBigPicture()

    /** InboxStyle — список строк, классика для почты. */
    fun showInbox()

    /** MessagingStyle — переписка с «персонами» и историей сообщений. */
    fun showMessaging()

    // ---- Поведение и обновление ----

    /** Показывает/обновляет уведомление загрузки: progress из [0, 100]. */
    fun showProgress(progress: Int)

    /** Индикатор загрузки неопределённой длительности. */
    fun showIndeterminate()

    /** Ongoing-уведомление — не смахивается, обновляется тем же id. */
    fun showOngoing()

    /** Обновляет текст ongoing-уведомления (повторный тап по кнопке). */
    fun updateOngoing()

    /** Стопка из 4 дочерних уведомлений + summary. */
    fun showGroup()

    // ---- Интерактив ----

    /** Уведомление с action-кнопкой «Прочитано» (BroadcastReceiver). */
    fun showActions()

    /** Уведомление с быстрым ответом (RemoteInput). */
    fun showReply()

    /** Кастомный layout через RemoteViews. */
    fun showCustom()

    /** Уведомление, которое придёт через [delayMs] (эмуляция отложенной доставки). */
    fun showDelayed(delayMs: Long)

    /** Отменяет уведомление по id (например, «Готово» после прогресса). */
    fun cancel(notificationId: Int)
}

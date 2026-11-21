package com.joy.sandbox.feature.notifications.impl.presentation

import androidx.lifecycle.viewModelScope
import com.joy.sandbox.core.ui.BaseViewModel
import com.joy.sandbox.core.ui.CommandFlow
import com.joy.sandbox.feature.notifications.impl.domain.LiveUpdateInteractor
import com.joy.sandbox.feature.notifications.impl.domain.LiveUpdateStage
import com.joy.sandbox.feature.notifications.impl.domain.NotificationsInteractor
import com.joy.sandbox.feature.notifications.impl.domain.PromotionStatus
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel экрана уведомлений: UI-agnostic, все вызовы — в [NotificationsInteractor].
 *
 * Уведомления здесь НЕ навигационные команды, а побочный эффект:
 * ViewModel просто дёргает интерактор. Команды используются только для
 * вещей, которые нельзя сделать без Activity/Composable-контекста
 * (запрос runtime-разрешения, открытие системных настроек).
 *
 * Методов много намеренно: тренажёр, один публичный метод = одна кнопка.
 */
@Suppress("TooManyFunctions")
class NotificationsViewModel @Inject constructor(
    private val interactor: NotificationsInteractor,
    private val liveUpdateInteractor: LiveUpdateInteractor,
) : BaseViewModel() {

    private var progressJob: Job? = null

    /** Job демо-сценария Live Update («доставка» из 5 этапов). */
    private var liveUpdateJob: Job? = null

    /** Показано ли ongoing-уведомление (для демо обновления по повторному тапу). */
    private var isOngoingShown = false

    val state = MutableStateFlow(State())

    val commandsFlow = CommandFlow<Commands>(viewModelScope)

    init {
        refreshPermissionState()
    }

    data class State(
        val isPermissionGranted: Boolean = false,
        /** Статус продвижения в Live Update (null — сценарий не запускался). */
        val liveUpdatePromotion: PromotionStatus? = null,
        val isLiveUpdateRunning: Boolean = false,
        val isLiveUpdateDismissedByUser: Boolean = false,
    )

    sealed interface Commands {

        /** Запустить системный диалог запроса POST_NOTIFICATIONS. */
        data object RequestPermission : Commands
    }

    /** Экран открылся: создаём каналы (idempotентно) и сверяем разрешение. */
    fun onScreenOpened() {
        interactor.onScreenOpened()
        refreshPermissionState()
    }

    fun onPermissionResult(granted: Boolean) {
        state.value = state.value.copy(isPermissionGranted = granted)
    }

    fun onRequestPermissionClick() {
        commandsFlow.tryEmit(Commands.RequestPermission)
    }

    // ---- Каналы ----

    fun onDynamicChannelClick() = interactor.createDynamicChannelAndNotify()

    fun onChannelSettingsClick() = interactor.openChannelSettings()

    // ---- Базовые виды ----

    fun onBasicClick() = interactor.showBasic()

    fun onHeadsUpClick() = interactor.showHeadsUp()

    fun onLowPriorityClick() = interactor.showLowPriority()

    // ---- Стили раскрытия ----

    fun onBigTextClick() = interactor.showBigText()

    fun onBigPictureClick() = interactor.showBigPicture()

    fun onInboxClick() = interactor.showInbox()

    fun onMessagingClick() = interactor.showMessaging()

    // ---- Поведение и обновление ----

    /** Эмуляция загрузки: тикаем прогресс с задержкой, обновляя ОДНО уведомление. */
    fun onProgressClick() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            interactor.showProgress(PROGRESS_START)
            var progress = PROGRESS_START
            while (progress < PROGRESS_MAX) {
                delay(PROGRESS_TICK_DELAY_MS)
                progress += PROGRESS_TICK_STEP
                interactor.showProgress(progress)
            }
        }
    }

    fun onIndeterminateClick() = interactor.showIndeterminate()

    /** Первый тап — показать ongoing; повторный — обновить его текст тем же id. */
    fun onOngoingClick() {
        if (isOngoingShown) {
            interactor.updateOngoing()
        } else {
            interactor.showOngoing()
            isOngoingShown = true
        }
    }

    fun onGroupClick() = interactor.showGroup()

    // ---- Интерактив ----

    fun onActionsClick() = interactor.showActions()

    fun onReplyClick() = interactor.showReply()

    fun onCustomClick() = interactor.showCustom()

    fun onDelayedClick() = interactor.showDelayed(DELAYED_NOTIFICATION_MS)

    // ---- Live Update (Android 16) ----

    /**
     * Демо-сценарий «доставка еды»: показывает Live Update и тикает
     * этапы таймером. Повторный тап останавливает сценарий и убирает
     * уведомление. Если пользователь сам убрал уведомление — тикаем
     * дальше нельзя (контракт Live Updates), останавливаемся.
     */
    fun onLiveUpdateClick() {
        if (liveUpdateJob != null) {
            stopLiveUpdateScenario(cancelNotification = true)
            return
        }
        liveUpdateJob = viewModelScope.launch {
            liveUpdateInteractor.show(LiveUpdateStage.ACCEPTED)
            state.value = state.value.copy(
                liveUpdatePromotion = liveUpdateInteractor.promotionStatus(),
                isLiveUpdateRunning = true,
                isLiveUpdateDismissedByUser = false,
            )
            var stage: LiveUpdateStage? = LiveUpdateStage.ACCEPTED
            while (stage != null) {
                delay(LIVE_UPDATE_STAGE_DELAY_MS)
                // Пользователь убрал уведомление — продолжать обновлять
                // нельзя (контракт Live Updates), единственный break здесь.
                if (liveUpdateInteractor.consumeUserDismissal()) {
                    state.value = state.value.copy(
                        isLiveUpdateRunning = false,
                        isLiveUpdateDismissedByUser = true,
                    )
                    break
                }
                stage = stage.next()
                stage?.let(liveUpdateInteractor::show)
            }
            liveUpdateJob = null
            state.value = state.value.copy(isLiveUpdateRunning = false)
        }
    }

    override fun onCleared() {
        // Запущенная «загрузка» не должна тикать для мёртвого экрана.
        progressJob?.cancel()
        // Live Update живёт своей жизнью в шторке — при уходе просто
        // прекращаем сценарий, но уведомление НЕ отменяем: пусть доживёт
        // до конца, как настоящий Live Update из реального приложения.
        stopLiveUpdateScenario(cancelNotification = false)
    }

    private fun stopLiveUpdateScenario(cancelNotification: Boolean) {
        liveUpdateJob?.cancel()
        liveUpdateJob = null
        if (cancelNotification) {
            liveUpdateInteractor.cancel()
        }
        state.value = state.value.copy(isLiveUpdateRunning = false)
    }

    private fun refreshPermissionState() {
        state.value = state.value.copy(isPermissionGranted = interactor.isPermissionGranted())
    }

    private companion object {

        const val PROGRESS_START = 0
        const val PROGRESS_MAX = 100
        const val PROGRESS_TICK_STEP = 5
        const val PROGRESS_TICK_DELAY_MS = 200L

        const val DELAYED_NOTIFICATION_MS = 5_000L

        /** Длительность этапа сценария доставки (совпадает с интерактором). */
        const val LIVE_UPDATE_STAGE_DELAY_MS = 5_000L
    }
}

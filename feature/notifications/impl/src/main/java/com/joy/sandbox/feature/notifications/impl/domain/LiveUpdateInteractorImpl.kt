package com.joy.sandbox.feature.notifications.impl.domain

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.joy.sandbox.feature.notifications.impl.R
import com.joy.sandbox.feature.notifications.impl.data.LiveUpdateDismissState
import com.joy.sandbox.feature.notifications.impl.data.NotificationIds
import com.joy.sandbox.feature.notifications.impl.receiver.NotificationActionReceiver
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Реализация Live Update: ongoing progress-style уведомление с сегментами,
 * точками пройденных этапов, tracker-иконкой и ETA.
 *
 * Продвижение (чип статус-бара + слот на экране блокировки) работает только
 * на Android 16 QPR1 (API 36.1) и выше и требует:
 * - разрешения POST_PROMOTED_NOTIFICATIONS (в манифесте app);
 * - ongoing + заголовок + ProgressStyle без кастомного RemoteViews;
 * - канала с IMPORTANCE_HIGH;
 * - включённой пользователем настройки «Live updates».
 * На API 36 (без QPR1) и ниже уведомление просто живёт в шторке и обновляется.
 */
@Singleton
class LiveUpdateInteractorImpl @Inject constructor(
    private val context: Context,
    private val notificationsInteractor: NotificationsInteractor,
) : LiveUpdateInteractor {

    private val notificationManager = NotificationManagerCompat.from(context)

    override fun show(stage: LiveUpdateStage) {
        if (!notificationsInteractor.isPermissionGranted()) return

        val builder = NotificationCompat.Builder(context, NotificationIds.CHANNEL_LIVE_UPDATES)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(stage.titleRes))
            .setContentText(context.getString(stage.textRes))
            .setContentIntent(createTapPendingIntent())
            // deleteIntent срабатывает и на свайп, и на «открепить» —
            // фиксируем, что пользователь сам убрал Live Update.
            .setDeleteIntent(createDismissPendingIntent())
            .setStyle(createProgressStyle(stage))
            // Без ongoing уведомление не продвигается и смахивается.
            .setOngoing(true)
            .setAutoCancel(false)
            .setOnlyAlertOnce(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
            configurePromotion(builder, stage)
        } else {
            // До Android 16 compat ProgressStyle рисует пустую полосу —
            // показываем классический детерминированный прогресс.
            builder.setProgress(PROGRESS_MAX, stage.progress, false)
        }

        if (stage == LiveUpdateStage.DELIVERED) {
            // Финал: после «доставлено» убираем карточку сами.
            builder.setTimeoutAfter(DELIVERED_TIMEOUT_MS)
        }

        notificationManager.notify(NotificationIds.LIVE_UPDATE, builder.build())
    }

    override fun cancel() {
        notificationManager.cancel(NotificationIds.LIVE_UPDATE)
    }

    override fun consumeUserDismissal(): Boolean = LiveUpdateDismissState.consumeDismissal()

    override fun promotionStatus(): PromotionStatus {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.BAKLAVA) {
            return PromotionStatus.UNSUPPORTED
        }
        // На прошивках Android 16.0 (до QPR1) метод может отсутствовать
        // или бросать исключение — оборачиваем в runCatching.
        val platformManager = context.getSystemService(NotificationManager::class.java)
        val canPost = runCatching { platformManager.canPostPromotedNotifications() }
            .getOrDefault(false)
        return if (canPost) PromotionStatus.AVAILABLE else PromotionStatus.NOT_ALLOWED
    }

    /** Часть builder'а, отвечающая за продвижение в Live Update (API 36+). */
    private fun configurePromotion(
        builder: NotificationCompat.Builder,
        stage: LiveUpdateStage,
    ) {
        builder
            // Просим систему продвинуть уведомление (чип статус-бара,
            // слот на lock screen). Само решение остаётся за системой
            // и настройкой пользователя.
            .setRequestPromotedOngoing(true)
            // Текст чипа статус-бара: очень короткий (запас — ~7 символов).
            .setShortCriticalText(context.getString(stage.shortCriticalTextRes))
        if (stage != LiveUpdateStage.DELIVERED) {
            // ETA: система сама ведёт обратный отсчёт.
            // ВАЖНО: when должен быть В БУДУЩЕМ, иначе система
            // пропускает обновление уведомления.
            builder
                .setShowWhen(true)
                .setWhen(System.currentTimeMillis() + remainingDurationMs(stage))
        }
    }

    /** ProgressStyle: сегменты полосы + точки пройденных этапов + tracker. */
    private fun createProgressStyle(stage: LiveUpdateStage): NotificationCompat.ProgressStyle {
        val style = NotificationCompat.ProgressStyle()
            .setStyledByProgress(true)
            .setProgressTrackerIcon(IconCompat.createWithResource(context, stage.trackerIconRes))
            .setProgress(stage.progress)
        SEGMENT_COLORS.forEachIndexed { index, _ ->
            // addProgressSegment: сегмент — кусок фона полосы.
            // setStyledByProgress(true) — закрашено то, что ПРОЙДЕНО.
            style.addProgressSegment(
                NotificationCompat.ProgressStyle.Segment(SEGMENT_LENGTH)
                    .setColor(Color.parseColor(SEGMENT_COLORS[index])),
            )
        }
        addPassedPoints(style, stage)
        return style
    }

    /** Точки на полосе — контрольные отметки уже пройденных этапов (≤ 4-х). */
    private fun addPassedPoints(
        style: NotificationCompat.ProgressStyle,
        stage: LiveUpdateStage,
    ) {
        LiveUpdateStage.entries
            .filter { passed -> passed.progress in 1 until stage.progress }
            .forEach { passed ->
                style.addProgressPoint(
                    NotificationCompat.ProgressStyle.Point(passed.progress)
                        .setColor(Color.parseColor(POINT_COLOR)),
                )
            }
    }

    /** Оставшееся время сценария от этапа [stage] — для ETA. */
    private fun remainingDurationMs(stage: LiveUpdateStage): Long {
        val stagesLeft = LiveUpdateStage.DELIVERED.ordinal - stage.ordinal
        return stagesLeft * DEMO_STAGE_DURATION_MS
    }

    /** Тап по Live Update открывает приложение (как у обычных уведомлений). */
    private fun createTapPendingIntent(): PendingIntent {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        return PendingIntent.getActivity(
            context,
            REQUEST_CODE_TAP,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    /** deleteIntent: пользователь убрал Live Update — запоминаем факт. */
    private fun createDismissPendingIntent(): PendingIntent {
        val intent = Intent(context, NotificationActionReceiver::class.java)
            .setAction(NotificationActionReceiver.ACTION_LIVE_UPDATE_DISMISSED)
        return PendingIntent.getBroadcast(
            context,
            NotificationIds.REQUEST_CODE_LIVE_UPDATE_DISMISSED,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private companion object {

        const val PROGRESS_MAX = 100
        const val SEGMENT_LENGTH = 25

        /** Длительность одного этапа демо-сценария (совпадает с ViewModel). */
        const val DEMO_STAGE_DURATION_MS = 5_000L

        /** Сколько «доставлено» висит перед автоотменой. */
        const val DELIVERED_TIMEOUT_MS = 5_000L

        const val REQUEST_CODE_TAP = 0

        /** Цвета 4-х сегментов полосы (высокий контраст обязателен). */
        val SEGMENT_COLORS = listOf("#3F51B5", "#7B1FA2", "#009688", "#FFA000")

        const val POINT_COLOR = "#FFFFFF"
    }
}

package com.joy.sandbox.feature.notifications.impl.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import com.joy.sandbox.feature.notifications.impl.R
import com.joy.sandbox.feature.notifications.impl.data.LiveUpdateDismissState
import com.joy.sandbox.feature.notifications.impl.data.NotificationIds

/**
 * Receiver для действий из уведомлений.
 *
 * Зачем BroadcastReceiver, а не Activity: нажатие на action-кнопку
 * не должно уводить пользователя из текущего приложения. Receiver
 * срабатывает в фоне, обрабатывает действие и сам завершается.
 *
 * Объявлен в манифесте impl-модуля (смержится в app). exported="false" —
 * внешние приложения не смогут его дернуть, только наши PendingIntent'ы.
 *
 * На Android 13+ (API 33) для показа/обновления уведомления из фона
 * тоже нужно разрешение POST_NOTIFICATIONS — ниже проверяем перед notify.
 */
class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_MARK_READ -> cancelNotification(context, NotificationIds.ACTIONS)
            ACTION_REPLY -> handleReply(context, intent)
            // Пользователь смахнул/открепил Live Update — сценарий
            // обязан прекратить обновления (требование системы).
            ACTION_LIVE_UPDATE_DISMISSED -> LiveUpdateDismissState.markDismissed()
        }
    }

    /** «Прочитано» — просто отменяем уведомление по его id. */
    private fun cancelNotification(context: Context, notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }

    /**
     * Ответ из шторки. Введённый текст прикрепляется к Intent'у как extra
     * благодаря RemoteInput — достаём его и ПЕРЕИСПОЛЬЗУЕМ id уведомления,
     * заменяя его содержимое на «Ответ отправлен».
     */
    private fun handleReply(context: Context, intent: Intent) {
        val results = RemoteInput.getResultsFromIntent(intent)
        val replyText = results?.getCharSequence(KEY_REPLY)?.toString()
        if (replyText.isNullOrBlank()) {
            cancelNotification(context, NotificationIds.REPLY)
            return
        }

        val notification = NotificationCompat.Builder(context, NotificationIds.CHANNEL_DEFAULT)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notifications_reply_sent_title))
            .setContentText(
                context.getString(R.string.notifications_reply_sent_text, replyText)
            )
            // Без таймаута обновлённое уведомление останется висеть —
            // отменяем его самостоятельно.
            .setTimeoutAfter(REPLY_RESULT_TIMEOUT_MS)
            .build()

        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            NotificationManagerCompat.from(context)
                .notify(NotificationIds.REPLY_RESULT, notification)
        }
    }

    companion object {

        const val ACTION_MARK_READ =
            "com.joy.sandbox.feature.notifications.impl.action.MARK_READ"

        const val ACTION_REPLY =
            "com.joy.sandbox.feature.notifications.impl.action.REPLY"

        /** Ключ, под которым RemoteInput кладёт введённый текст в Intent. */
        const val KEY_REPLY = "sandbox_notifications_key_reply"

        /** Отмена Live Update пользователем (deleteIntent). */
        const val ACTION_LIVE_UPDATE_DISMISSED =
            "com.joy.sandbox.feature.notifications.impl.action.LIVE_UPDATE_DISMISSED"

        private const val REPLY_RESULT_TIMEOUT_MS = 5_000L
    }
}

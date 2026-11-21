package com.joy.sandbox.feature.notifications.impl.data

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import com.joy.sandbox.feature.notifications.impl.R
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Каналы уведомлений — с Android 8.0 (API 26) каждое уведомление обязано
 * принадлежать каналу, и именно КАНАЛ определяет звук, вибрацию и поведение
 * (heads-up или тихо в шторке). Настройки канала пользователь может менять
 * в системных настройках приложения — отменить его выбор из кода нельзя.
 *
 * minSdk проекта = 26, поэтому legacy-ветки для API < 26 не нужны.
 */
@Singleton
class NotificationChannels @Inject constructor(
    private val context: Context,
) {

    /** Счётчик для id динамических каналов (созданных «из кода» по кнопке). */
    private val dynamicChannelCounter = AtomicInteger(0)

    /**
     * Создаёт «базовые» каналы, если их ещё нет. Вызов не разрушит
     * пользовательские настройки существующего канала — повторное создание
     * канала с тем же id просто игнорируется.
     */
    fun ensureBaseChannels() {
        createChannel(
            id = NotificationIds.CHANNEL_DEFAULT,
            name = context.getString(R.string.notifications_channel_default_name),
            description = context.getString(R.string.notifications_channel_default_description),
            importance = NotificationManager.IMPORTANCE_DEFAULT,
        )
        // HIGH — единственный уровень, при котором уведомление «выпадает»
        // поверх экрана (heads-up), если не включён режим «Не беспокоить».
        createChannel(
            id = NotificationIds.CHANNEL_HIGH,
            name = context.getString(R.string.notifications_channel_high_name),
            description = context.getString(R.string.notifications_channel_high_description),
            importance = NotificationManager.IMPORTANCE_HIGH,
        )
        // LOW — без звука и вибрации, молча добавляется в шторку.
        createChannel(
            id = NotificationIds.CHANNEL_LOW,
            name = context.getString(R.string.notifications_channel_low_name),
            description = context.getString(R.string.notifications_channel_low_description),
            importance = NotificationManager.IMPORTANCE_LOW,
        )
        // Live Updates: продвигаемые ongoing-уведомления требуют канала
        // с IMPORTANCE_HIGH, иначе hasPromotableCharacteristics() = false.
        createChannel(
            id = NotificationIds.CHANNEL_LIVE_UPDATES,
            name = context.getString(R.string.notifications_channel_live_updates_name),
            description = context.getString(R.string.notifications_channel_live_updates_description),
            importance = NotificationManager.IMPORTANCE_HIGH,
        )
    }

    /**
     * Динамическое создание канала — например, когда канал нужен под конкретный
     * чат/тему. Вернёт id нового канала; канал сразу появится в настройках
     * приложения (Android 8+ показывает их везде, где есть уведомления).
     */
    fun createDynamicChannel(): String {
        val index = dynamicChannelCounter.incrementAndGet()
        val id = NotificationIds.CHANNEL_DYNAMIC_PREFIX + index
        createChannel(
            id = id,
            name = "Динамический канал #$index",
            description = null,
            importance = NotificationManager.IMPORTANCE_DEFAULT,
        )
        return id
    }

    /** Открывает системный экран настроек конкретного канала. */
    fun openChannelSettings(channelId: String) {
        val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            putExtra(Settings.EXTRA_CHANNEL_ID, channelId)
            // Из контекста приложения (не Activity) обязателен флаг новой задачи.
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private fun createChannel(
        id: String,
        name: String,
        description: String?,
        importance: Int,
    ) {
        // Через NotificationChannelCompat канал создаётся без обращения
        // к android.app.NotificationChannel напрямую — compat-вариант.
        val builder = NotificationChannelCompat.Builder(id, importance)
            .setName(name)
        if (description != null) {
            builder.setDescription(description)
        }
        // Compat-вариант менеджера принимает NotificationChannelCompat напрямую.
        NotificationManagerCompat.from(context).createNotificationChannel(builder.build())
    }
}

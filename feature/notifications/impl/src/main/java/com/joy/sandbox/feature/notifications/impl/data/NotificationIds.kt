package com.joy.sandbox.feature.notifications.impl.data

/**
 * Единое место для id уведомлений и ключей каналов.
 *
 * Почему это важно:
 * - Показ уведомления с тем же id = обновление существующего (не плодим копии);
 * - Receiver (кнопки действий, reply) и interactor должны ссылаться на одни id;
 * - Для отмены уведомления нужен тот же id, с которым его показали.
 */
object NotificationIds {

    // ---- Каналы ----
    const val CHANNEL_DEFAULT = "sandbox_notifications_default"
    const val CHANNEL_HIGH = "sandbox_notifications_high"
    const val CHANNEL_LOW = "sandbox_notifications_low"
    /** Live Updates: ongoing progress-style, продвигаемое на lock screen (API 36.1). */
    const val CHANNEL_LIVE_UPDATES = "sandbox_notifications_live_updates"

    /** Префикс каналов, создаваемых из кода (динамический id = префикс + счётчик). */
    const val CHANNEL_DYNAMIC_PREFIX = "sandbox_notifications_dynamic_"

    // ---- Уведомления ----
    const val BASIC = 1001
    const val HEADS_UP = 1002
    const val LOW = 1003
    const val DYNAMIC_CHANNEL = 1004
    const val BIG_TEXT = 1005
    const val BIG_PICTURE = 1006
    const val INBOX = 1007
    const val MESSAGING = 1008
    const val PROGRESS = 1009
    const val INDETERMINATE = 1010
    const val ONGOING = 1011
    const val GROUP_SUMMARY = 1012
    const val ACTIONS = 1013
    const val REPLY = 1014
    const val CUSTOM = 1015
    const val DELAYED = 1016
    const val LIVE_UPDATE = 1017

    // Дочерние уведомления группы должны иметь РАЗНЫЕ id.
    const val GROUP_CHILD_FIRST = 1101
    const val GROUP_CHILD_LAST = 1104

    // Ответ из receiver обновляет это же уведомление.
    const val REPLY_RESULT = 1014

    /** Ключ группы — общий для дочерних уведомлений и summary. */
    const val GROUP_KEY_DEMO = "sandbox_notifications_group_demo"

    /** requestCode для PendingIntent'ов receiver'а — должен быть уникален на action. */
    const val REQUEST_CODE_MARK_READ = 1
    const val REQUEST_CODE_REPLY = 2
    const val REQUEST_CODE_LIVE_UPDATE_DISMISSED = 3
}

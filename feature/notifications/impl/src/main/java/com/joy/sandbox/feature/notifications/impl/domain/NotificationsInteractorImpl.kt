package com.joy.sandbox.feature.notifications.impl.domain

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.os.Build
import android.widget.RemoteViews
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import androidx.core.content.ContextCompat
import com.joy.sandbox.feature.notifications.impl.R
import com.joy.sandbox.feature.notifications.impl.data.NotificationChannels
import com.joy.sandbox.feature.notifications.impl.data.NotificationIds
import com.joy.sandbox.feature.notifications.impl.receiver.NotificationActionReceiver
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Реализация [NotificationsInteractor]: здесь живут все NotificationCompat.Builder'ы.
 *
 * Ключевые моменты, которые стоит запомнить:
 * - minSdk = 26, поэтому каналы обязательны, а legacy-кода под API < 26 нет;
 * - notify() с уже известным id обновляет существующее уведомление;
 * - с API 31 (Android 12) у каждого PendingIntent обязан быть флаг
 *   FLAG_IMMUTABLE или FLAG_MUTABLE — иначе crash;
 * - с API 33 (Android 13) нужно runtime-разрешение POST_NOTIFICATIONS.
 *
 * Методов много намеренно: это тренажёр, один метод = один вид уведомления,
 * все builder'ы рядом, чтобы было удобно сравнивать.
 */
@Suppress("TooManyFunctions")
@Singleton
class NotificationsInteractorImpl @Inject constructor(
    private val context: Context,
    private val channels: NotificationChannels,
) : NotificationsInteractor {

    private val notificationManager = NotificationManagerCompat.from(context)

    /** Scope для отложенного показа: ViewModel может погибнуть, а уведомление должно прийти. */
    private val delayedScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // ------------------------------------------------------------------ служебное

    override fun onScreenOpened() {
        channels.ensureBaseChannels()
    }

    override fun isPermissionGranted(): Boolean = canNotify()

    override fun cancel(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }

    override fun openChannelSettings() {
        channels.openChannelSettings(NotificationIds.CHANNEL_DEFAULT)
    }

    /**
     * Общий предохранитель перед notify: без разрешения (или если пользователь
     * выключил уведомления в настройках) notify() либо упадёт с SecurityException
     * (API 33+), либо молча не покажет ничего.
     */
    private fun canNotify(): Boolean {
        if (!notificationManager.areNotificationsEnabled()) return false
        // POST_NOTIFICATIONS появилось только в API 33 — на младших версиях
        // пермиссии в манифесте достаточно.
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
    }

    /** Стартовый builder: small icon и tap-action одинаковы почти для всех видов. */
    private fun baseBuilder(channelId: String): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(createTapPendingIntent())
            // Автоотмена: уведомление исчезает по тапу, а не остаётся в шторке.
            .setAutoCancel(true)
    }

    /**
     * Tap по уведомлению открывает лаунчер-activity приложения.
     * getLaunchIntentForPackage() не требует зависимости на :app —
     * берём intent, который система сама строит для нашего пакета.
     */
    private fun createTapPendingIntent(): PendingIntent {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?.apply {
                putExtra(EXTRA_FROM_NOTIFICATION, true)
                // Чтобы новый запуск не плодил стек поверх старого.
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
        return PendingIntent.getActivity(
            context,
            REQUEST_CODE_TAP,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    @SuppressLint("MissingPermission")
    private fun notify(notificationId: Int, builder: NotificationCompat.Builder) {
        if (canNotify()) {
            notificationManager.notify(notificationId, builder.build())
        }
    }

    // ------------------------------------------------------------------ каналы

    override fun createDynamicChannelAndNotify() {
        val channelId = channels.createDynamicChannel()
        val builder = baseBuilder(channelId)
            .setContentTitle(context.getString(R.string.notifications_channel_created_title))
            .setContentText(context.getString(R.string.notifications_channel_created_text))
        notify(NotificationIds.DYNAMIC_CHANNEL, builder)
    }

    // ------------------------------------------------------------------ базовые виды

    override fun showBasic() {
        val builder = baseBuilder(NotificationIds.CHANNEL_DEFAULT)
            .setContentTitle(context.getString(R.string.notifications_basic_title))
            .setContentText(context.getString(R.string.notifications_basic_text))
            // Бейдж (число на иконке лаунчера). Многие лаунчеры рисуют вместо
            // числа просто точку, но число всё равно стоит задавать.
            .setNumber(BADGE_NUMBER)
        notify(NotificationIds.BASIC, builder)
    }

    override fun showHeadsUp() {
        // Heads-up зависит ТОЛЬКО от importance канала — вызовом
        // из кода «выпрыгнуть поверх экрана» не заставить.
        val builder = baseBuilder(NotificationIds.CHANNEL_HIGH)
            .setContentTitle(context.getString(R.string.notifications_heads_up_title))
            .setContentText(context.getString(R.string.notifications_heads_up_text))
        notify(NotificationIds.HEADS_UP, builder)
    }

    override fun showLowPriority() {
        val builder = baseBuilder(NotificationIds.CHANNEL_LOW)
            .setContentTitle(context.getString(R.string.notifications_low_title))
            .setContentText(context.getString(R.string.notifications_low_text))
        notify(NotificationIds.LOW, builder)
    }

    // ------------------------------------------------------------------ стили раскрытия

    override fun showBigText() {
        val style = NotificationCompat.BigTextStyle()
            .bigText(context.getString(R.string.notifications_big_text_content))
            // summaryText — подпись под раскрытым текстом.
            .setSummaryText(context.getString(R.string.notifications_big_text_summary))
        val builder = baseBuilder(NotificationIds.CHANNEL_DEFAULT)
            .setContentTitle(context.getString(R.string.notifications_big_text_title))
            .setContentText(context.getString(R.string.notifications_big_text_summary))
            .setStyle(style)
        notify(NotificationIds.BIG_TEXT, builder)
    }

    override fun showBigPicture() {
        val style = NotificationCompat.BigPictureStyle()
            .bigPicture(createDemoBitmap())
            // bigLargeIcon — что показать вместо картинки после свайпа обратно.
            .bigLargeIcon(null as Bitmap?)
            .setSummaryText(context.getString(R.string.notifications_big_picture_summary))
        val builder = baseBuilder(NotificationIds.CHANNEL_DEFAULT)
            .setContentTitle(context.getString(R.string.notifications_big_picture_title))
            .setContentText(context.getString(R.string.notifications_big_picture_summary))
            .setStyle(style)
        notify(NotificationIds.BIG_PICTURE, builder)
    }

    override fun showInbox() {
        val style = NotificationCompat.InboxStyle()
            .addLine(context.getString(R.string.notifications_inbox_line_1))
            .addLine(context.getString(R.string.notifications_inbox_line_2))
            .addLine(context.getString(R.string.notifications_inbox_line_3))
        val builder = baseBuilder(NotificationIds.CHANNEL_DEFAULT)
            .setContentTitle(context.getString(R.string.notifications_inbox_title))
            .setContentText(context.getString(R.string.notifications_inbox_line_1))
            .setStyle(style)
        notify(NotificationIds.INBOX, builder)
    }

    override fun showMessaging() {
        val me = Person.Builder().setName(context.getString(R.string.notifications_messaging_me)).build()
        val friend = Person.Builder()
            .setName(context.getString(R.string.notifications_messaging_friend))
            .build()
        val now = System.currentTimeMillis()
        // MessagingStyle прячет историю под «статусом набора» — добавляем
        // сообщения в хронологическом порядке, таймстемпы реальные.
        val style = NotificationCompat.MessagingStyle(me)
            .setConversationTitle(context.getString(R.string.notifications_messaging_title))
            .addMessage(
                context.getString(R.string.notifications_messaging_msg_1),
                now - MESSAGE_FIRST_AGE_MS,
                friend,
            )
            .addMessage(
                context.getString(R.string.notifications_messaging_msg_2),
                now - MESSAGE_SECOND_AGE_MS,
                me,
            )
            .addMessage(
                context.getString(R.string.notifications_messaging_msg_3),
                now - MESSAGE_THIRD_AGE_MS,
                friend,
            )
        val builder = baseBuilder(NotificationIds.CHANNEL_DEFAULT)
            .setContentTitle(context.getString(R.string.notifications_messaging_title))
            .setStyle(style)
        notify(NotificationIds.MESSAGING, builder)
    }

    // ------------------------------------------------------------------ поведение и обновление

    override fun showProgress(progress: Int) {
        // При 100% меняем текст на «Готово» и ставим автоотмену —
        // отдельно cancel() дергать не нужно.
        val isDone = progress >= PROGRESS_MAX
        val textRes = if (isDone) {
            R.string.notifications_progress_done
        } else {
            R.string.notifications_progress_text
        }
        val builder = baseBuilder(NotificationIds.CHANNEL_DEFAULT)
            .setContentTitle(context.getString(R.string.notifications_progress_title))
            .setContentText(context.getString(textRes, progress))
            // Ключевой вызов для обновляемых уведомлений: не дёргать
            // звук/вибрацию при каждом тике прогресса.
            .setOnlyAlertOnce(true)
            // Определённый прогресс: max = 100, текущее значение, не indeterminate.
            .setProgress(PROGRESS_MAX, progress, false)
        if (isDone) {
            builder.setTimeoutAfter(PROGRESS_DONE_TIMEOUT_MS)
        }
        notify(NotificationIds.PROGRESS, builder)
    }

    override fun showIndeterminate() {
        val builder = baseBuilder(NotificationIds.CHANNEL_LOW)
            .setContentTitle(context.getString(R.string.notifications_indeterminate_title))
            .setContentText(context.getString(R.string.notifications_indeterminate_text))
            // Третий аргумент true — «полоска бегает», прогресс неизвестен.
            .setProgress(PROGRESS_MAX, PROGRESS_START, true)
        notify(NotificationIds.INDETERMINATE, builder)
    }

    override fun showOngoing() {
        val builder = baseBuilder(NotificationIds.CHANNEL_DEFAULT)
            .setContentTitle(context.getString(R.string.notifications_ongoing_title))
            .setContentText(context.getString(R.string.notifications_ongoing_text_first))
            // Ongoing: пользователь не сможет смахнуть уведомление,
            // только отменить его через крестик или действие.
            .setOngoing(true)
        notify(NotificationIds.ONGOING, builder)
    }

    override fun updateOngoing() {
        val builder = baseBuilder(NotificationIds.CHANNEL_DEFAULT)
            .setContentTitle(context.getString(R.string.notifications_ongoing_title))
            .setContentText(context.getString(R.string.notifications_ongoing_text_updated))
            .setOngoing(true)
        // Тот же id — обновляем, а не создаём второе уведомление.
        notify(NotificationIds.ONGOING, builder)
    }

    override fun showGroup() {
        // Сначала дочерние уведомления с общим group key.
        for (index in GROUP_CHILD_INDEXES) {
            val builder = baseBuilder(NotificationIds.CHANNEL_DEFAULT)
                .setContentTitle(
                    context.getString(R.string.notifications_group_child_title) + " #" + index
                )
                .setContentText(context.getString(groupChildTextRes(index)))
                .setGroup(NotificationIds.GROUP_KEY_DEMO)
            notify(NotificationIds.GROUP_CHILD_FIRST + index - 1, builder)
        }
        // Summary — «обложка» стопки. На API 24+ без summary дочерние
        // уведомления тоже стянутся, но summary даёт заголовок стопке.
        val summary = baseBuilder(NotificationIds.CHANNEL_DEFAULT)
            .setContentTitle(context.getString(R.string.notifications_group_summary_title))
            .setContentText(context.getString(R.string.notifications_group_summary_text))
            .setGroup(NotificationIds.GROUP_KEY_DEMO)
            .setGroupSummary(true)
        notify(NotificationIds.GROUP_SUMMARY, summary)
    }

    // ------------------------------------------------------------------ интерактив

    override fun showActions() {
        val markReadIntent = Intent(context, NotificationActionReceiver::class.java)
            .setAction(NotificationActionReceiver.ACTION_MARK_READ)
        val markReadPendingIntent = PendingIntent.getBroadcast(
            context,
            NotificationIds.REQUEST_CODE_MARK_READ,
            markReadIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val builder = baseBuilder(NotificationIds.CHANNEL_DEFAULT)
            .setContentTitle(context.getString(R.string.notifications_actions_title))
            .setContentText(context.getString(R.string.notifications_actions_text))
            // Action: иконка + подпись + PendingIntent (здесь — broadcast в receiver).
            .addAction(
                R.drawable.ic_notification,
                context.getString(R.string.notifications_actions_mark_read),
                markReadPendingIntent,
            )
        notify(NotificationIds.ACTIONS, builder)
    }

    override fun showReply() {
        val remoteInput = RemoteInput.Builder(NotificationActionReceiver.KEY_REPLY)
            .setLabel(context.getString(R.string.notifications_reply_hint))
            .build()
        val replyIntent = Intent(context, NotificationActionReceiver::class.java)
            .setAction(NotificationActionReceiver.ACTION_REPLY)
        val replyPendingIntent = PendingIntent.getBroadcast(
            context,
            NotificationIds.REQUEST_CODE_REPLY,
            replyIntent,
            // Единственное место, где нужен FLAG_MUTABLE: система дописывает
            // в Intent введённый текст (RemoteInput) перед вызовом receiver'а.
            // FLAG_IMMUTABLE здесь запрещён — notify() падает с IllegalArgumentException.
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
        )
        val replyAction = NotificationCompat.Action.Builder(
            R.drawable.ic_notification,
            context.getString(R.string.notifications_reply_action),
            replyPendingIntent,
        )
            .addRemoteInput(remoteInput)
            .build()
        val builder = baseBuilder(NotificationIds.CHANNEL_DEFAULT)
            .setContentTitle(context.getString(R.string.notifications_reply_title))
            .setContentText(context.getString(R.string.notifications_reply_text))
            .addAction(replyAction)
        notify(NotificationIds.REPLY, builder)
    }

    override fun showCustom() {
        val collapsed = RemoteViews(context.packageName, R.layout.view_notification_custom)
        val expanded = RemoteViews(context.packageName, R.layout.view_notification_custom_expanded)
        val builder = baseBuilder(NotificationIds.CHANNEL_DEFAULT)
            .setContentTitle(context.getString(R.string.notifications_custom_title))
            .setContentText(context.getString(R.string.notifications_custom_text))
            // DecoratedCustomViewStyle оставляет стандартные «плашки»
            // (иконку приложения, время) вокруг нашего layout.
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(collapsed)
            .setCustomBigContentView(expanded)
        notify(NotificationIds.CUSTOM, builder)
    }

    override fun showDelayed(delayMs: Long) {
        // В реальном приложении здесь был бы WorkManager/AlarmManager
        // (доставка даже после перезагрузки и при убитом процессе).
        delayedScope.launch {
            delay(delayMs)
            val builder = baseBuilder(NotificationIds.CHANNEL_DEFAULT)
                .setContentTitle(context.getString(R.string.notifications_delayed_title))
                .setContentText(context.getString(R.string.notifications_delayed_text))
            notify(NotificationIds.DELAYED, builder)
        }
    }

    // ------------------------------------------------------------------ приватные

    /** Тексты дочерних уведомлений группы — каждому индексу свой ресурс. */
    private fun groupChildTextRes(index: Int): Int {
        return GROUP_CHILD_TEXTS[index - 1]
    }

    /** Демо-картинка для BigPictureStyle — рисуем градиент с текстом, без ассетов. */
    private fun createDemoBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(BITMAP_WIDTH, BITMAP_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.shader = LinearGradient(
            0f,
            0f,
            BITMAP_WIDTH.toFloat(),
            BITMAP_HEIGHT.toFloat(),
            Color.parseColor(GRADIENT_START_COLOR),
            Color.parseColor(GRADIENT_END_COLOR),
            Shader.TileMode.CLAMP,
        )
        canvas.drawRect(0f, 0f, BITMAP_WIDTH.toFloat(), BITMAP_HEIGHT.toFloat(), paint)
        paint.shader = null
        paint.isAntiAlias = true
        paint.color = Color.WHITE
        paint.textSize = BITMAP_TEXT_SIZE_PX
        canvas.drawText(BITMAP_TEXT, BITMAP_TEXT_OFFSET_X_PX, BITMAP_TEXT_OFFSET_Y_PX, paint)
        return bitmap
    }

    private companion object {

        /** Extra для MainActivity: тап пришёл из уведомления. */
        const val EXTRA_FROM_NOTIFICATION = "sandbox_notifications_from_notification"

        const val REQUEST_CODE_TAP = 0

        const val BADGE_NUMBER = 7

        const val MESSAGE_FIRST_AGE_MS = 180_000L
        const val MESSAGE_SECOND_AGE_MS = 120_000L
        const val MESSAGE_THIRD_AGE_MS = 60_000L
        const val PROGRESS_MAX = 100
        const val PROGRESS_START = 0
        const val PROGRESS_DONE_TIMEOUT_MS = 3_000L

        const val BITMAP_WIDTH = 1024
        const val BITMAP_HEIGHT = 512
        const val BITMAP_TEXT_SIZE_PX = 96f
        const val BITMAP_TEXT_OFFSET_X_PX = 64f
        const val BITMAP_TEXT_OFFSET_Y_PX = 288f
        const val BITMAP_TEXT = "SandBox"
        const val GRADIENT_START_COLOR = "#FF3F51B5"
        const val GRADIENT_END_COLOR = "#FF009688"

        // Индексы дочерних уведомлений группы: id = GROUP_CHILD_FIRST + index - 1.
        val GROUP_CHILD_INDEXES = 1..4

        /** Тексты дочерних уведомлений группы по индексу (index - 1). */
        val GROUP_CHILD_TEXTS = listOf(
            R.string.notifications_inbox_line_1,
            R.string.notifications_inbox_line_2,
            R.string.notifications_inbox_line_3,
            R.string.notifications_messaging_msg_3,
        )
    }
}

package com.joy.sandbox.feature.notifications.impl.domain

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.joy.sandbox.feature.notifications.impl.R

// Значения прогресса этапов — top-level const, чтобы их можно было
// использовать в инициализаторах enum-entries (компаньон инициализируется
// ПОСЛЕ entries, ссылаться на него из них нельзя).
private const val PROGRESS_ACCEPTED = 0
private const val PROGRESS_PREPARING = 25
private const val PROGRESS_EN_ROUTE = 50
private const val PROGRESS_ARRIVING = 75
private const val PROGRESS_DELIVERED = 100

/**
 * Этап демо-сценария «доставка еды» для Live Update.
 *
 * Прогресс-бар нотификации собран из сегментов (общая длина ≤ 100);
 * точки (Point) — контрольные отметки на полосе (не больше 4-х).
 * На каждом этапе видны точки ПРЕДЫДУЩИХ этапов — так система рисует
 * «пройденный путь».
 */
enum class LiveUpdateStage(
    val progress: Int,
    @DrawableRes val trackerIconRes: Int,
    @StringRes val titleRes: Int,
    @StringRes val textRes: Int,
    @StringRes val shortCriticalTextRes: Int,
) {

    ACCEPTED(
        progress = PROGRESS_ACCEPTED,
        trackerIconRes = R.drawable.ic_live_update_accepted,
        titleRes = R.string.notifications_live_update_title_accepted,
        textRes = R.string.notifications_live_update_text_accepted,
        shortCriticalTextRes = R.string.notifications_live_update_chip_accepted,
    ),
    PREPARING(
        progress = PROGRESS_PREPARING,
        trackerIconRes = R.drawable.ic_live_update_preparing,
        titleRes = R.string.notifications_live_update_title_preparing,
        textRes = R.string.notifications_live_update_text_preparing,
        shortCriticalTextRes = R.string.notifications_live_update_chip_preparing,
    ),
    EN_ROUTE(
        progress = PROGRESS_EN_ROUTE,
        trackerIconRes = R.drawable.ic_live_update_en_route,
        titleRes = R.string.notifications_live_update_title_en_route,
        textRes = R.string.notifications_live_update_text_en_route,
        shortCriticalTextRes = R.string.notifications_live_update_chip_en_route,
    ),
    ARRIVING(
        progress = PROGRESS_ARRIVING,
        trackerIconRes = R.drawable.ic_live_update_arriving,
        titleRes = R.string.notifications_live_update_title_arriving,
        textRes = R.string.notifications_live_update_text_arriving,
        shortCriticalTextRes = R.string.notifications_live_update_chip_arriving,
    ),
    DELIVERED(
        progress = PROGRESS_DELIVERED,
        trackerIconRes = R.drawable.ic_live_update_delivered,
        titleRes = R.string.notifications_live_update_title_delivered,
        textRes = R.string.notifications_live_update_text_delivered,
        shortCriticalTextRes = R.string.notifications_live_update_chip_delivered,
    ),
    ;

    /** Следующий этап; для DELIVERED — null (сценарий окончен). */
    fun next(): LiveUpdateStage? = entries.getOrNull(ordinal + 1)
}

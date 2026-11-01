package com.joy.sandbox.feature.websocket.impl.remotedatasource.model

/**
 * Ответ Yahoo Finance spark API по одному символу.
 *
 * Ответ выглядит как JSON-объект, ключи которого — тикеры:
 * {"AAPL": { ... }, "BTC-USD": { ... }}
 */
data class SparkResponse(
    /** Время начала каждой свечи, unix-секунды. Индексы совпадают с [close]. */
    val timestamp: List<Long>? = null,
    /** Цена закрытия каждой свечи. Индексы совпадают с [timestamp]; свечи без торгов — null. */
    val close: List<Double?>? = null,
    /** Изменение цены за день в процентах относительно закрытия предыдущего дня. */
    val fulldayChangePercent: Double? = null,
)

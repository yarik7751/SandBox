package com.joy.sandbox.feature.websocket.impl.domain.model

/**
 * Параметры подписки на цены и снапшота.
 *
 * @param symbols тикеры для подписки.
 * @param interval шаг свечей снапшота (параметр `interval` spark-запроса).
 * @param range глубина данных снапшота (параметр `range` spark-запроса).
 */
data class PriceSnapshotParams(
    val symbols: List<String>,
    val interval: String = DEFAULT_INTERVAL,
    val range: String = DEFAULT_RANGE,
) {

    private companion object {
        const val DEFAULT_INTERVAL = "5m"
        const val DEFAULT_RANGE = "1d"
    }
}

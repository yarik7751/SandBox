package com.joy.sandbox.feature.websocket.impl.remotedatasource.model

/**
 * Ответ Yahoo Finance spark API по одному символу.
 */
data class SparkResponse(
    val timestamp: List<Long>? = null,
    val close: List<Double?>? = null,
    val fulldayChangePercent: Double? = null,
)

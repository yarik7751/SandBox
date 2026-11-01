package com.joy.sandbox.feature.websocket.impl.remotedatasource.model

data class PriceDto(
    val id: String,
    val price: Float,
    val timeMillis: Long,
    val currency: String,
    val exchange: String,
    val quoteType: QuoteType,
    val marketHours: MarketHoursType,
    val changePercent: Float,
    val dayVolume: Long,
    val dayHigh: Float,
    val dayLow: Float,
    val change: Float,
    val shortName: String,
    val marketClosed: Boolean = false,
)

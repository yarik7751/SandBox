package com.joy.sandbox.feature.websocket.impl.data.model

data class PriceDataModel(
    val symbol: String,
    val price: Double,
    val changePercent: Float,
    val marketClosed: Boolean,
)

package com.joy.sandbox.feature.websocket.impl.domain.model

data class Price(
    val symbol: String,
    val price: Double,
    val changePercent: Float,
    val marketClosed: Boolean,
)

package com.joy.sandbox.feature.websocket.impl.data.model

data class ProductDataModel(
    val id: Long,
    val title: String,
    val price: Double? = null,
)

package com.joy.sandbox.feature.websocket.impl.domain.model

/**
 * Источник данных по продуктам.
 *
 * @param id идентификатор источника.
 * @param socketUrl адрес WebSocket-сокета.
 * @param symbols все возможные значения (тикеры), доступные для подписки на этом сокете.
 */
data class ProductsDataSource(
    val id: String,
    val socketUrl: String,
    val symbols: List<String>,
)

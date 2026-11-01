package com.joy.sandbox.feature.websocket.impl.domain.model

/**
 * Доступные продукты источника данных.
 *
 * @param id идентификатор источника.
 * @param socketUrl адрес WebSocket-сокета.
 * @param symbols все возможные значения (тикеры), доступные для подписки на этом сокете.
 */
data class AvailableProducts(
    val id: String,
    val socketUrl: String,
    val symbols: List<String>,
)

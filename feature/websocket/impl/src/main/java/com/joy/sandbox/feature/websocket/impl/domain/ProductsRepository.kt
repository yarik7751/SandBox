package com.joy.sandbox.feature.websocket.impl.domain

import com.joy.sandbox.feature.websocket.impl.domain.model.AvailableProducts
import com.joy.sandbox.feature.websocket.impl.domain.model.Price
import com.joy.sandbox.feature.websocket.impl.domain.model.PriceSnapshotParams
import com.joy.sandbox.feature.websocket.impl.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductsRepository {

    fun observeAddedProducts(): Flow<List<Product>>

    fun getAvailableSymbols(): AvailableProducts

    fun observePrices(): Flow<Price>

    fun updatePriceSubscription(params: PriceSnapshotParams)

    fun disconnectPrices()

    suspend fun addProduct(symbol: String)

    suspend fun removeProduct(id: Long)
}

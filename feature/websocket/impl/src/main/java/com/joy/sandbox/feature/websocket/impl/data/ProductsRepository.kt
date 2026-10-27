package com.joy.sandbox.feature.websocket.impl.data

import com.joy.sandbox.feature.websocket.impl.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductsRepository {

    fun observeAddedProducts(): Flow<List<Product>>

    fun getAvailableSymbols(): List<String>

    suspend fun addProduct(symbol: String)

    suspend fun removeProduct(id: Long)
}

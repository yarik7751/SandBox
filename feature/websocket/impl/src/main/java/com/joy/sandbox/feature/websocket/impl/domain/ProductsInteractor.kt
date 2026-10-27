package com.joy.sandbox.feature.websocket.impl.domain

import com.joy.sandbox.feature.websocket.impl.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductsInteractor {

    fun observeProducts(): Flow<List<Product>>
}

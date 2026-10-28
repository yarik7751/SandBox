package com.joy.sandbox.feature.websocket.impl.domain

import com.joy.sandbox.feature.websocket.impl.domain.model.AvailableProducts
import com.joy.sandbox.feature.websocket.impl.domain.model.Product
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ProductsInteractorImpl @Inject constructor(
    private val productsRepository: ProductsRepository,
) : ProductsInteractor {

    override fun observeProducts(): Flow<List<Product>> {
        return productsRepository.observeAddedProducts()
    }

    override fun getAvailableSymbols(): AvailableProducts {
        return productsRepository.getAvailableSymbols()
    }

    override suspend fun addProduct(symbol: String) {
        productsRepository.addProduct(symbol)
    }

    override suspend fun removeProduct(id: Long) {
        productsRepository.removeProduct(id)
    }
}

package com.joy.sandbox.feature.websocket.impl.data

import com.joy.sandbox.feature.websocket.impl.data.db.ProductEntity
import com.joy.sandbox.feature.websocket.impl.data.db.ProductsDao
import com.joy.sandbox.feature.websocket.impl.domain.ProductsLocalDataSourceProvider
import com.joy.sandbox.feature.websocket.impl.domain.model.AvailableProducts
import com.joy.sandbox.feature.websocket.impl.domain.model.Product
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProductsRepositoryImpl @Inject constructor(
    private val productsDao: ProductsDao,
    private val localDataSourceProvider: ProductsLocalDataSourceProvider,
) : ProductsRepository {

    override fun observeAddedProducts(): Flow<List<Product>> {
        return productsDao.observeAll().map { entities ->
            entities.map { entity ->
                Product(
                    id = entity.id,
                    title = entity.symbol,
                    price = null,
                )
            }
        }
    }

    override fun getAvailableSymbols(): AvailableProducts {
        return localDataSourceProvider.provide()
    }

    override suspend fun addProduct(symbol: String) {
        productsDao.insert(ProductEntity(symbol = symbol))
    }

    override suspend fun removeProduct(id: Long) {
        productsDao.deleteById(id)
    }
}

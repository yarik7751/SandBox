package com.joy.sandbox.feature.websocket.impl.data

import com.joy.sandbox.feature.websocket.impl.data.mapper.ProductDataModelMapper
import com.joy.sandbox.feature.websocket.impl.data.mapper.ProductMapper
import com.joy.sandbox.feature.websocket.impl.domain.ProductsRepository
import com.joy.sandbox.feature.websocket.impl.domain.model.AvailableProducts
import com.joy.sandbox.feature.websocket.impl.domain.model.Product
import com.joy.sandbox.feature.websocket.impl.localdatasource.ProductsLocalDataSourceProvider
import com.joy.sandbox.feature.websocket.impl.localdatasource.db.ProductEntity
import com.joy.sandbox.feature.websocket.impl.localdatasource.db.ProductsDao
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProductsRepositoryImpl @Inject constructor(
    private val productsDao: ProductsDao,
    private val localDataSourceProvider: ProductsLocalDataSourceProvider,
    private val productDataModelMapper: ProductDataModelMapper,
    private val productMapper: ProductMapper,
) : ProductsRepository {

    override fun observeAddedProducts(): Flow<List<Product>> {
        return productsDao.observeAll().map { entities ->
            entities.map { entity ->
                productMapper.map(productDataModelMapper.map(entity))
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

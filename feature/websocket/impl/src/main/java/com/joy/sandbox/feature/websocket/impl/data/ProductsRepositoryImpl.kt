package com.joy.sandbox.feature.websocket.impl.data

import com.joy.sandbox.feature.websocket.impl.data.mapper.PriceDataModelMapper
import com.joy.sandbox.feature.websocket.impl.data.mapper.PriceMapper
import com.joy.sandbox.feature.websocket.impl.data.mapper.ProductDataModelMapper
import com.joy.sandbox.feature.websocket.impl.data.mapper.ProductMapper
import com.joy.sandbox.feature.websocket.impl.domain.ProductsRepository
import com.joy.sandbox.feature.websocket.impl.domain.model.AvailableProducts
import com.joy.sandbox.feature.websocket.impl.domain.model.Price
import com.joy.sandbox.feature.websocket.impl.domain.model.PriceSnapshotParams
import com.joy.sandbox.feature.websocket.impl.domain.model.Product
import com.joy.sandbox.feature.websocket.impl.localdatasource.ProductsLocalDataSourceProvider
import com.joy.sandbox.feature.websocket.impl.localdatasource.db.ProductEntity
import com.joy.sandbox.feature.websocket.impl.localdatasource.db.ProductsDao
import com.joy.sandbox.feature.websocket.impl.remotedatasource.PricesRemoteDataSource
import com.joy.sandbox.feature.websocket.impl.remotedatasource.PricesSnapshotRemoteDataSource
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch

class ProductsRepositoryImpl @Inject constructor(
    private val productsDao: ProductsDao,
    private val localDataSourceProvider: ProductsLocalDataSourceProvider,
    private val productDataModelMapper: ProductDataModelMapper,
    private val productMapper: ProductMapper,
    private val pricesRemoteDataSource: PricesRemoteDataSource,
    private val pricesSnapshotRemoteDataSource: PricesSnapshotRemoteDataSource,
    private val priceDataModelMapper: PriceDataModelMapper,
    private val priceMapper: PriceMapper,
) : ProductsRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val snapshotPricesFlow = MutableSharedFlow<Price>(extraBufferCapacity = PRICES_BUFFER)
    private var subscribedSymbols: Set<String> = emptySet()

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

    override fun observePrices(): Flow<Price> {
        return merge(
            pricesRemoteDataSource.pricesFlow.map { dto ->
                priceMapper.map(priceDataModelMapper.map(dto))
            },
            snapshotPricesFlow,
        )
    }

    override fun updatePriceSubscription(params: PriceSnapshotParams) {
        pricesRemoteDataSource.updateSubscription(params.symbols)

        val newSymbols = params.symbols.toSet() - subscribedSymbols
        subscribedSymbols = params.symbols.toSet()
        if (newSymbols.isEmpty()) return

        scope.launch {
            pricesSnapshotRemoteDataSource
                .fetchPrices(newSymbols.toList(), params.interval, params.range)
                .map { dto -> priceMapper.map(priceDataModelMapper.map(dto)) }
                .forEach { price -> snapshotPricesFlow.emit(price) }
        }
    }

    override fun disconnectPrices() {
        pricesRemoteDataSource.disconnect()
    }

    override suspend fun addProduct(symbol: String) {
        productsDao.insert(ProductEntity(symbol = symbol))
    }

    override suspend fun removeProduct(id: Long) {
        productsDao.deleteById(id)
    }

    private companion object {
        const val PRICES_BUFFER = 64
    }
}

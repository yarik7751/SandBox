package com.joy.sandbox.feature.websocket.impl.domain

import com.joy.sandbox.feature.websocket.impl.domain.model.ProductsDataSource

interface ProductsDataSourceProvider {

    fun provide(): ProductsDataSource
}

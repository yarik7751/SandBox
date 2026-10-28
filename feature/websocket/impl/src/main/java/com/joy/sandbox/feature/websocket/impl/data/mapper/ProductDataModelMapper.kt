package com.joy.sandbox.feature.websocket.impl.data.mapper

import com.joy.sandbox.feature.websocket.impl.data.model.ProductDataModel
import com.joy.sandbox.feature.websocket.impl.domain.model.Product
import com.joy.sandbox.feature.websocket.impl.localdatasource.db.ProductEntity
import javax.inject.Inject

class ProductDataModelMapper @Inject constructor() {

    fun map(entity: ProductEntity): ProductDataModel {
        return ProductDataModel(
            id = entity.id,
            title = entity.symbol,
        )
    }
}

class ProductMapper @Inject constructor() {

    fun map(dataModel: ProductDataModel): Product {
        return Product(
            id = dataModel.id,
            title = dataModel.title,
            price = dataModel.price,
        )
    }
}

package com.joy.sandbox.feature.websocket.impl.data.mapper

import com.joy.sandbox.feature.websocket.impl.data.model.PriceDataModel
import com.joy.sandbox.feature.websocket.impl.domain.model.Price
import com.joy.sandbox.feature.websocket.impl.remotedatasource.model.PriceDto
import javax.inject.Inject

class PriceDataModelMapper @Inject constructor() {

    fun map(dto: PriceDto): PriceDataModel {
        return PriceDataModel(
            symbol = dto.id,
            price = dto.price.toDouble(),
            changePercent = dto.changePercent,
            marketClosed = dto.marketClosed,
        )
    }
}

class PriceMapper @Inject constructor() {

    fun map(dataModel: PriceDataModel): Price {
        return Price(
            symbol = dataModel.symbol,
            price = dataModel.price,
            changePercent = dataModel.changePercent,
            marketClosed = dataModel.marketClosed,
        )
    }
}

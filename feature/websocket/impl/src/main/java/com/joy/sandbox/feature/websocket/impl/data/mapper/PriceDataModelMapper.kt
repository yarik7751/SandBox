package com.joy.sandbox.feature.websocket.impl.data.mapper

import com.joy.sandbox.feature.websocket.impl.data.model.PriceDataModel
import com.joy.sandbox.feature.websocket.impl.domain.model.Price
import com.joy.sandbox.feature.websocket.impl.remotedatasource.model.PriceDto
import com.joy.sandbox.feature.websocket.impl.remotedatasource.model.SparkResponse
import java.util.concurrent.TimeUnit
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

    /**
     * Конвертирует spark-ответ в модель данных.
     *
     * Берёт последнюю непустую свечу. [PriceDataModel.marketClosed] — эвристика:
     * рынок считается закрытым, если свеча старше [MARKET_CLOSED_THRESHOLD_MINUTES]
     * (закрытая биржа перестаёт обновлять свечи; крипта ходит 24/7).
     *
     * @return null, если по символу нет ни одной непустой свечи.
     */
    fun map(symbol: String, response: SparkResponse): PriceDataModel? {
        val timestamps = response.timestamp ?: return null
        val closes = response.close ?: return null

        // Ищем последнюю непустую свечу.
        var lastIndex = -1
        for (i in closes.lastIndex downTo 0) {
            if (closes[i] != null) {
                lastIndex = i
                break
            }
        }
        if (lastIndex < 0 || lastIndex >= timestamps.size) return null

        val lastClose = closes[lastIndex] ?: return null
        val lastTimestampSec = timestamps[lastIndex]
        val ageMillis = System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(lastTimestampSec)

        return PriceDataModel(
            symbol = symbol,
            price = lastClose,
            changePercent = response.fulldayChangePercent?.toFloat() ?: 0f,
            marketClosed = ageMillis > MARKET_CLOSED_THRESHOLD_MILLIS,
        )
    }

    private companion object {
        const val MARKET_CLOSED_THRESHOLD_MINUTES = 15L
        val MARKET_CLOSED_THRESHOLD_MILLIS = TimeUnit.MINUTES.toMillis(MARKET_CLOSED_THRESHOLD_MINUTES)
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

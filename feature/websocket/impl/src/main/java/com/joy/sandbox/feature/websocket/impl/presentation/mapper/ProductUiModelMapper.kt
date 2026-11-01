package com.joy.sandbox.feature.websocket.impl.presentation.mapper

import com.joy.sandbox.feature.websocket.impl.domain.model.Price
import com.joy.sandbox.feature.websocket.impl.domain.model.Product
import com.joy.sandbox.feature.websocket.impl.presentation.model.PriceDirection
import com.joy.sandbox.feature.websocket.impl.presentation.model.ProductUiModel
import java.util.Locale
import javax.inject.Inject

class ProductUiModelMapper @Inject constructor() {

    fun map(product: Product, price: Price?): ProductUiModel {
        return ProductUiModel(
            id = product.id,
            title = product.title,
            price = formatPrice(price?.price ?: product.price),
            direction = direction(price),
        )
    }

    private fun direction(price: Price?): PriceDirection {
        return when {
            price == null -> PriceDirection.NONE
            price.marketClosed -> PriceDirection.CLOSED
            price.changePercent < 0 -> PriceDirection.DOWN
            price.changePercent > 0 -> PriceDirection.UP
            else -> PriceDirection.NONE
        }
    }

    private fun formatPrice(price: Double?): String {
        return price?.let { String.format(Locale.US, "%.2f ₽", it) } ?: "—"
    }
}

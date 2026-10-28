package com.joy.sandbox.feature.websocket.impl.presentation.mapper

import com.joy.sandbox.feature.websocket.impl.domain.model.Product
import com.joy.sandbox.feature.websocket.impl.presentation.model.ProductUiModel
import java.util.Locale
import javax.inject.Inject

class ProductUiModelMapper @Inject constructor() {

    fun map(product: Product): ProductUiModel {
        return ProductUiModel(
            id = product.id,
            title = product.title,
            price = formatPrice(product.price),
        )
    }

    private fun formatPrice(price: Double?): String {
        return price?.let { String.format(Locale.US, "%.2f ₽", it) } ?: "—"
    }
}

package com.joy.sandbox.feature.websocket.impl.domain

import com.joy.sandbox.feature.websocket.impl.domain.model.Product
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class ProductsInteractorImpl @Inject constructor() : ProductsInteractor {

    override fun observeProducts(): Flow<List<Product>> = flowOf(MOCK_PRODUCTS)

    private companion object {

        val MOCK_PRODUCTS = listOf(
            Product(id = 1, title = "Кофе зерновой", price = 1290.0),
            Product(id = 2, title = "Чай зелёный", price = 540.5),
            Product(id = 3, title = "Мёд натуральный", price = 890.0),
            Product(id = 4, title = "Орехи микс", price = 760.9),
            Product(id = 5, title = "Шоколад тёмный", price = 320.0),
            Product(id = 6, title = "Макароны", price = 145.6),
            Product(id = 7, title = "Рис басмати", price = 410.0),
            Product(id = 8, title = "Оливковое масло", price = 1150.0),
            Product(id = 9, title = "Сыр пармезан", price = 1980.0),
            Product(id = 10, title = "Хлеб цельнозерновой", price = 180.0),
            Product(id = 11, title = "Яблоки сезонные", price = 220.4),
            Product(id = 12, title = "Бананы", price = 160.0),
            Product(id = 13, title = "Молоко фермерское", price = 260.0),
            Product(id = 14, title = "Йогурт греческий", price = 340.8),
            Product(id = 15, title = "Яйца С0", price = 390.0),
            Product(id = 16, title = "Курица охлаждённая", price = 890.5),
            Product(id = 17, title = "Лосось филе", price = 2340.0),
            Product(id = 18, title = "Креветки в/м", price = 1780.0),
            Product(id = 19, title = "Вода минеральная", price = 95.0),
            Product(id = 20, title = "Сок яблочный", price = 310.0),
        )
    }
}

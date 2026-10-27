package com.joy.sandbox.feature.websocket.impl.domain

import com.joy.sandbox.feature.websocket.impl.domain.model.ProductsDataSource
import javax.inject.Inject

/**
 * Провайдер источника Yahoo Finance.
 *
 * Сокет wss://streamer.finance.yahoo.com/?version=2 принимает подписку вида
 * {"subscribe": ["AAPL", "BTC-USD", ...]}, где значениями являются тикеры Yahoo Finance:
 * акции (AAPL), индексы (^GSPC), фьючерсы (CL=F), валютные пары (EURUSD=X), криптовалюты (BTC-USD).
 * Сокет допускает подписку на любой тикер Yahoo Finance — ниже перечислены все
 * основные категории с типовыми значениями каждой.
 */
class YahooProductsDataSourceProvider @Inject constructor() : ProductsDataSourceProvider {

    override fun provide(): ProductsDataSource {
        return ProductsDataSource(
            id = ID,
            socketUrl = SOCKET_URL,
            symbols = SYMBOLS,
        )
    }

    private companion object {

        const val ID = "yahoo_finance"
        const val SOCKET_URL = "wss://streamer.finance.yahoo.com/?version=2"

        val SYMBOLS = buildList {
            // Индексы
            addAll(
                listOf(
                    "^GSPC", "^DJI", "^IXIC", "^RUT", "^VIX", "^TNX",
                    "^FTSE", "^N225", "^CMC200", "^GDAXI", "^FCHI",
                )
            )
            // Фьючерсы
            addAll(
                listOf(
                    "ES=F", "YM=F", "NQ=F", "RTY=F",
                    "CL=F", "GC=F", "SI=F", "BZ=F", "NG=F",
                )
            )
            // Валютные пары
            addAll(
                listOf(
                    "EURUSD=X", "GBPUSD=X", "USDJPY=X", "EURRUB=X", "USDRUB=X",
                )
            )
            // Криптовалюты
            addAll(
                listOf(
                    "BTC-USD", "ETH-USD", "USDT-USD", "SOL-USD", "XRP-USD", "DOGE-USD",
                )
            )
            // Акции
            addAll(
                listOf(
                    "AAPL", "MSFT", "GOOGL", "AMZN", "NVDA", "META", "TSLA", "BRK-B",
                    "JPM", "V", "UNH", "XOM", "JNJ", "WMT", "PG", "MA", "HD", "CVX",
                    "LLY", "ABBV", "PFE", "KO", "PEP", "COST", "MRK", "TMO", "AVGO",
                    "ORCL", "CSCO", "ACN", "NFLX", "AMD", "INTC", "NKE", "DIS", "BA",
                )
            )
        }
    }
}

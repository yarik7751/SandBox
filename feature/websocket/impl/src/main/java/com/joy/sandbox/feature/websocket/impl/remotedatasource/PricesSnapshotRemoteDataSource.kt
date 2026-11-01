package com.joy.sandbox.feature.websocket.impl.remotedatasource

import android.util.Log
import com.joy.sandbox.feature.websocket.impl.remotedatasource.api.YahooFinanceApi
import com.joy.sandbox.feature.websocket.impl.remotedatasource.model.MarketHoursType
import com.joy.sandbox.feature.websocket.impl.remotedatasource.model.PriceDto
import com.joy.sandbox.feature.websocket.impl.remotedatasource.model.QuoteType
import com.joy.sandbox.feature.websocket.impl.remotedatasource.model.SparkResponse
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Источник снапшотов цен через REST Yahoo Finance (v8/finance/spark).
 *
 * Один запрос принимает несколько тикеров через запятую. Рынок считается закрытым,
 * если последняя свеча старше [MARKET_CLOSED_THRESHOLD_MINUTES] — для закрытой
 * биржи свечи перестают обновляться, крипта ходит 24/7.
 */
class PricesSnapshotRemoteDataSource @Inject constructor(
    private val yahooFinanceApi: YahooFinanceApi,
) {

    suspend fun fetchPrices(
        symbols: List<String>,
        interval: String,
        range: String,
    ): List<PriceDto> = withContext(Dispatchers.IO) {
        if (symbols.isEmpty()) return@withContext emptyList()

        val response = try {
            yahooFinanceApi.fetchSpark(symbols.joinToString(","), range, interval)
        } catch (e: IOException) {
            Log.w(TAG, "fetchPrices network error: ${e.message}")
            return@withContext emptyList()
        }
        if (!response.isSuccessful) {
            Log.w(TAG, "fetchPrices: code=${response.code()}")
            return@withContext emptyList()
        }

        parse(response.body(), symbols)
    }

    private fun parse(
        body: Map<String, SparkResponse>?,
        symbols: List<String>,
    ): List<PriceDto> {
        if (body == null) return emptyList()
        return symbols.mapNotNull { symbol -> parseSymbol(body[symbol], symbol) }
    }

    private fun parseSymbol(data: SparkResponse?, symbol: String): PriceDto? {
        val timestamps = data?.timestamp ?: return null
        val closes = data.close ?: return null

        // Берём последнюю непустую свечу.
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

        return PriceDto(
            id = symbol,
            price = lastClose.toFloat(),
            timeMillis = TimeUnit.SECONDS.toMillis(lastTimestampSec),
            currency = "",
            exchange = "",
            quoteType = QuoteType.NONE,
            marketHours = MarketHoursType.UNKNOWN,
            changePercent = data.fulldayChangePercent?.toFloat() ?: 0f,
            dayVolume = 0L,
            dayHigh = 0f,
            dayLow = 0f,
            change = 0f,
            shortName = "",
            marketClosed = ageMillis > MARKET_CLOSED_THRESHOLD_MILLIS,
        )
    }

    private companion object {
        const val TAG = "PricesSnapshotRemoteDataSource"
        const val MARKET_CLOSED_THRESHOLD_MINUTES = 15L
        val MARKET_CLOSED_THRESHOLD_MILLIS = TimeUnit.MINUTES.toMillis(MARKET_CLOSED_THRESHOLD_MINUTES)
    }
}

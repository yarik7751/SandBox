package com.joy.sandbox.feature.websocket.impl.remotedatasource

import android.util.Log
import com.joy.sandbox.feature.websocket.impl.remotedatasource.model.MarketHoursType
import com.joy.sandbox.feature.websocket.impl.remotedatasource.model.PriceDto
import com.joy.sandbox.feature.websocket.impl.remotedatasource.model.QuoteType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Источник снапшотов цен через REST Yahoo Finance (v8/finance/spark).
 *
 * Один запрос принимает несколько тикеров через запятую. Рынок считается закрытым,
 * если последняя свеча старше [MARKET_CLOSED_THRESHOLD_MINUTES] — для закрытой
 * биржи свечи перестают обновляться, крипта ходит 24/7.
 */
class PricesSnapshotRemoteDataSource @Inject constructor(
    private val okHttpClient: OkHttpClient,
) {

    suspend fun fetchPrices(
        symbols: List<String>,
        interval: String,
        range: String,
    ): List<PriceDto> = withContext(Dispatchers.IO) {
        if (symbols.isEmpty()) return@withContext emptyList()

        val url = "$SPARK_URL?${PARAM_SYMBOLS}=${symbols.joinToString(",")}" +
            "&$PARAM_RANGE=$range&$PARAM_INTERVAL=$interval"
        val request = Request.Builder()
            .url(url)
            .header(HEADER_USER_AGENT, USER_AGENT)
            .build()

        runCatching {
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.w(TAG, "fetchPrices: code=${response.code}")
                    return@use emptyList()
                }
                parse(response.body.string(), symbols)
            }
        }.onFailure { Log.e(TAG, "fetchPrices: ${it.message}", it) }
            .getOrDefault(emptyList())
    }

    private fun parse(json: String, symbols: List<String>): List<PriceDto> {
        val root = runCatching { JSONObject(json) }.getOrNull() ?: return emptyList()
        return symbols.mapNotNull { symbol -> parseSymbol(root, symbol) }
    }

    private fun parseSymbol(root: JSONObject, symbol: String): PriceDto? {
        val data = root.optJSONObject(symbol) ?: return null
        val timestamps = data.optJSONArray(JSON_KEY_TIMESTAMP) ?: return null
        val closes = data.optJSONArray(JSON_KEY_CLOSE) ?: return null

        // Берём последнюю непустую свечу.
        var lastIndex = -1
        for (i in closes.length() - 1 downTo 0) {
            if (!closes.isNull(i)) {
                lastIndex = i
                break
            }
        }
        if (lastIndex < 0) return null

        val lastClose = closes.getDouble(lastIndex)
        val lastTimestampSec = timestamps.getLong(lastIndex)
        val ageMillis = System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(lastTimestampSec)
        val changePercent = data.optDouble(JSON_KEY_FULLDAY_CHANGE_PERCENT, 0.0).toFloat()

        return PriceDto(
            id = symbol,
            price = lastClose.toFloat(),
            timeMillis = TimeUnit.SECONDS.toMillis(lastTimestampSec),
            currency = "",
            exchange = "",
            quoteType = QuoteType.NONE,
            marketHours = MarketHoursType.UNKNOWN,
            changePercent = changePercent,
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
        const val SPARK_URL = "https://query1.finance.yahoo.com/v8/finance/spark"
        const val PARAM_SYMBOLS = "symbols"
        const val PARAM_RANGE = "range"
        const val PARAM_INTERVAL = "interval"
        const val HEADER_USER_AGENT = "User-Agent"
        const val USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36"
        const val JSON_KEY_TIMESTAMP = "timestamp"
        const val JSON_KEY_CLOSE = "close"
        const val JSON_KEY_FULLDAY_CHANGE_PERCENT = "fulldayChangePercent"
        const val MARKET_CLOSED_THRESHOLD_MINUTES = 15L
        val MARKET_CLOSED_THRESHOLD_MILLIS = TimeUnit.MINUTES.toMillis(MARKET_CLOSED_THRESHOLD_MINUTES)
    }
}

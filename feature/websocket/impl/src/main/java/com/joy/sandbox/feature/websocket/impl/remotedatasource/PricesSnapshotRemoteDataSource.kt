package com.joy.sandbox.feature.websocket.impl.remotedatasource

import android.util.Log
import com.joy.sandbox.feature.websocket.impl.remotedatasource.api.YahooFinanceApi
import com.joy.sandbox.feature.websocket.impl.remotedatasource.model.SparkResponse
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Источник снапшотов цен через REST Yahoo Finance (v8/finance/spark).
 *
 * Тонкий клиент: один пакетный запрос на все символы, без конвертации в модель
 * данных — отдаёт ответ API как есть (см. [SparkResponse]).
 */
class PricesSnapshotRemoteDataSource @Inject constructor(
    private val yahooFinanceApi: YahooFinanceApi,
) {

    /**
     * Пакетный снапшот котировок.
     *
     * @param symbols тикеры для запроса.
     * @param interval параметр `interval` spark-запроса, шаг свечей.
     * @param range параметр `range` spark-запроса, глубина истории.
     *
     * @return карта "тикер → ответ spark API"; null при сетевой ошибке или не-2xx.
     */
    suspend fun fetchPrices(
        symbols: List<String>,
        interval: String,
        range: String,
    ): Map<String, SparkResponse>? = withContext(Dispatchers.IO) {
        if (symbols.isEmpty()) return@withContext null

        val response = try {
            yahooFinanceApi.fetchSpark(symbols.joinToString(","), range, interval)
        } catch (e: IOException) {
            Log.w(TAG, "fetchPrices network error: ${e.message}")
            return@withContext null
        }
        if (!response.isSuccessful) {
            Log.w(TAG, "fetchPrices: code=${response.code()}")
            return@withContext null
        }
        response.body()
    }

    private companion object {
        const val TAG = "PricesSnapshotRemoteDataSource"
    }
}

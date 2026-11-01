package com.joy.sandbox.feature.websocket.impl.remotedatasource.api

import com.joy.sandbox.feature.websocket.impl.remotedatasource.model.SparkResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface YahooFinanceApi {

    /**
     * GET v8/finance/spark — пакетный снапшот котировок.
     *
     * @param symbols тикеры Yahoo Finance через запятую ("AAPL,BTC-USD"); регистр важен.
     * @param range глубина истории: 1d, 5d, 1mo, 3mo, 6mo, 1y, 5y, max.
     * @param interval шаг свечей: 1m, 5m, 15m, 30m, 1h, 1d, 1wk, 1mo.
     *   Ограничение: interval не должен быть крупнее ~range/150.
     *
     * @return карта "тикер → данные"; отсутствующий ключ = по символу нет данных.
     *   Не-2xx коды приходят в [Response.isSuccessful] == false, тело при этом null.
     */
    @GET("v8/finance/spark")
    suspend fun fetchSpark(
        @Query("symbols") symbols: String,
        @Query("range") range: String,
        @Query("interval") interval: String,
    ): Response<Map<String, SparkResponse>>
}

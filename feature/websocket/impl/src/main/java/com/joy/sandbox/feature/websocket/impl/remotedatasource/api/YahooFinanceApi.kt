package com.joy.sandbox.feature.websocket.impl.remotedatasource.api

import com.joy.sandbox.feature.websocket.impl.remotedatasource.model.SparkResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface YahooFinanceApi {

    @GET("v8/finance/spark")
    suspend fun fetchSpark(
        @Query("symbols") symbols: String,
        @Query("range") range: String,
        @Query("interval") interval: String,
    ): Response<Map<String, SparkResponse>>
}

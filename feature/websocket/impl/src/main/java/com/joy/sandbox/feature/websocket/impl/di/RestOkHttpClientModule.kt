package com.joy.sandbox.feature.websocket.impl.di

import com.joy.sandbox.feature.websocket.impl.di.qualifier.RestOkHttpClient
import com.joy.sandbox.feature.websocket.impl.remotedatasource.api.YahooFinanceApi
import dagger.Module
import dagger.Provides
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
class RestOkHttpClientModule {

    @Provides
    @Singleton
    @RestOkHttpClient
    fun provideRestOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            // Yahoo отвечает 429 на запросы без User-Agent.
            .addInterceptor { chain ->
                chain.proceed(
                    chain.request()
                        .newBuilder()
                        .header(HEADER_USER_AGENT, USER_AGENT)
                        .build(),
                )
            }
            .build()
    }

    @Provides
    @Singleton
    fun provideRestRetrofit(@RestOkHttpClient okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideYahooFinanceApi(retrofit: Retrofit): YahooFinanceApi {
        return retrofit.create(YahooFinanceApi::class.java)
    }

    private companion object {
        const val TIMEOUT_SECONDS = 10L
        const val BASE_URL = "https://query1.finance.yahoo.com/"
        const val HEADER_USER_AGENT = "User-Agent"
        const val USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36"
    }
}

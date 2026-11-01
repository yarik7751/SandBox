package com.joy.sandbox.feature.websocket.impl.di

import com.joy.sandbox.feature.websocket.impl.di.qualifier.WebSocketOkHttpClient
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class WebSocketOkHttpClientModule {

    @Provides
    @Singleton
    @WebSocketOkHttpClient
    fun provideWebSocketOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .pingInterval(PING_INTERVAL_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    private companion object {
        const val PING_INTERVAL_SECONDS = 15L
    }
}

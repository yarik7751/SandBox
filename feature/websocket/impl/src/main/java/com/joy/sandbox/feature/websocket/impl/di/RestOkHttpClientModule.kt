package com.joy.sandbox.feature.websocket.impl.di

import com.joy.sandbox.feature.websocket.impl.di.qualifier.RestOkHttpClient
import dagger.Module
import dagger.Provides
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import okhttp3.OkHttpClient

@Module
class RestOkHttpClientModule {

    @Provides
    @Singleton
    @RestOkHttpClient
    fun provideRestOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    private companion object {
        const val TIMEOUT_SECONDS = 10L
    }
}

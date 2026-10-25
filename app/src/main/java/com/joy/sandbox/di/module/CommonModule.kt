package com.joy.sandbox.di.module

import android.content.Context
import androidx.core.os.ConfigurationCompat
import dagger.Module
import dagger.Provides
import java.util.Locale
import javax.inject.Singleton

@Module
class CommonModule {

    @Provides
    @Singleton
    fun provideLocale(context: Context): Locale {
        return ConfigurationCompat.getLocales(
            context.resources.configuration
        ).get(0) ?: context.resources.configuration.locales.get(0)
    }
}

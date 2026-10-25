package com.joy.sandbox.di.module

import com.joy.sandbox.utils.date.DateFormatter
import com.joy.sandbox.utils.date.DateFormatterImpl
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
interface DateTimeModule {

    @Binds
    @Singleton
    fun provideDateFormatter(impl: DateFormatterImpl): DateFormatter
}

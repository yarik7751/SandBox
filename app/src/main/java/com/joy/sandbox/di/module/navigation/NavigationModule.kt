package com.joy.sandbox.di.module.navigation

import androidx.lifecycle.ViewModel
import com.joy.sandbox.core.navigation.router.CurrentArgs
import com.joy.sandbox.core.navigation.router.JoyRouter
import com.joy.sandbox.core.navigation.router.ScreensFlow
import com.joy.sandbox.core.ui.ViewModelFactory
import dagger.Module
import dagger.Provides
import javax.inject.Provider
import javax.inject.Singleton

@Module
class NavigationModule {

    @Provides
    @Singleton
    fun provideJoyRouter(): JoyRouter = JoyRouter()

    @Provides
    fun provideScreensFlow(router: JoyRouter): ScreensFlow = router

    @Provides
    fun provideCurrentArgs(router: JoyRouter): CurrentArgs = router

    @Provides
    @Singleton
    fun provideViewModelFactory(
        creators: Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>,
    ): ViewModelFactory = ViewModelFactory(creators)
}

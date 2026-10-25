package com.joy.sandbox.feature.home.impl

import androidx.lifecycle.ViewModel
import com.joy.sandbox.core.navigation.ScreenContentProvider
import com.joy.sandbox.core.ui.ScreenKey
import com.joy.sandbox.core.ui.ViewModelKey
import com.joy.sandbox.feature.home.api.HomeJoyScreen
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap

@Module
class HomeModule {

    @Provides
    @IntoMap
    @ViewModelKey(HomeViewModel::class)
    fun provideHomeViewModel(): ViewModel = HomeViewModel()

    @Provides
    @IntoMap
    @ScreenKey(HomeJoyScreen.ROUTE)
    fun provideHomeContent(): ScreenContentProvider = HomeScreenProvider()
}

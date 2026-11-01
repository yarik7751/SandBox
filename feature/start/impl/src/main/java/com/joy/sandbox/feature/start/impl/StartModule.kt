package com.joy.sandbox.feature.start.impl

import androidx.lifecycle.ViewModel
import com.joy.sandbox.core.navigation.ScreenContentProvider
import com.joy.sandbox.core.ui.ScreenKey
import com.joy.sandbox.core.ui.ViewModelKey
import com.joy.sandbox.feature.start.api.StartJoyScreen
import com.joy.sandbox.feature.start.impl.presentation.StartScreenProvider
import com.joy.sandbox.feature.start.impl.presentation.StartViewModel
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap

@Module
class StartModule {

    @Provides
    @IntoMap
    @ViewModelKey(StartViewModel::class)
    fun provideStartViewModel(): ViewModel = StartViewModel()

    @Provides
    @IntoMap
    @ScreenKey(StartJoyScreen.ROUTE)
    fun provideStartContent(): ScreenContentProvider = StartScreenProvider()
}

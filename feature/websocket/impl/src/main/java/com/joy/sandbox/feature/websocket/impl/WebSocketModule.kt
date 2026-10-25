package com.joy.sandbox.feature.websocket.impl

import androidx.lifecycle.ViewModel
import com.joy.sandbox.core.navigation.ScreenContentProvider
import com.joy.sandbox.core.ui.ScreenKey
import com.joy.sandbox.core.ui.ViewModelKey
import com.joy.sandbox.feature.websocket.api.WebSocketJoyScreen
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap

@Module
class WebSocketModule {

    @Provides
    @IntoMap
    @ViewModelKey(WebSocketViewModel::class)
    fun provideWebSocketViewModel(viewModel: WebSocketViewModel): ViewModel = viewModel

    @Provides
    @IntoMap
    @ScreenKey(WebSocketJoyScreen.ROUTE)
    fun provideWebSocketContent(): ScreenContentProvider = WebSocketScreenProvider()
}

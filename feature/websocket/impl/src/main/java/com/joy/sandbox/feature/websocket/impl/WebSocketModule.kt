package com.joy.sandbox.feature.websocket.impl

import androidx.lifecycle.ViewModel
import com.joy.sandbox.core.navigation.ScreenContentProvider
import com.joy.sandbox.core.ui.ScreenKey
import com.joy.sandbox.core.ui.ViewModelKey
import com.joy.sandbox.feature.websocket.api.WebSocketJoyScreen
import com.joy.sandbox.feature.websocket.impl.domain.ProductsDataSourceProvider
import com.joy.sandbox.feature.websocket.impl.domain.ProductsInteractor
import com.joy.sandbox.feature.websocket.impl.domain.ProductsInteractorImpl
import com.joy.sandbox.feature.websocket.impl.domain.YahooProductsDataSourceProvider
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap

@Module
class WebSocketModule {

    @Provides
    fun provideProductsInteractor(impl: ProductsInteractorImpl): ProductsInteractor = impl

    @Provides
    fun provideProductsDataSourceProvider(
        impl: YahooProductsDataSourceProvider,
    ): ProductsDataSourceProvider = impl

    @Provides
    @IntoMap
    @ViewModelKey(WebSocketViewModel::class)
    fun provideWebSocketViewModel(viewModel: WebSocketViewModel): ViewModel = viewModel

    @Provides
    @IntoMap
    @ScreenKey(WebSocketJoyScreen.ROUTE)
    fun provideWebSocketContent(): ScreenContentProvider = WebSocketScreenProvider()
}

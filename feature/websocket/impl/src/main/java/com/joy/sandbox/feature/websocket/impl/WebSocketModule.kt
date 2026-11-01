package com.joy.sandbox.feature.websocket.impl

import androidx.lifecycle.ViewModel
import com.joy.sandbox.core.navigation.ScreenContentProvider
import com.joy.sandbox.core.ui.ScreenKey
import com.joy.sandbox.core.ui.ViewModelKey
import com.joy.sandbox.feature.websocket.api.AddProductsJoyScreen
import com.joy.sandbox.feature.websocket.api.WebSocketJoyScreen
import com.joy.sandbox.feature.websocket.impl.domain.ProductsRepository
import com.joy.sandbox.feature.websocket.impl.data.ProductsRepositoryImpl
import com.joy.sandbox.feature.websocket.impl.domain.ProductsInteractor
import com.joy.sandbox.feature.websocket.impl.domain.ProductsInteractorImpl
import com.joy.sandbox.feature.websocket.impl.presentation.addproducts.AddProductsScreenProvider
import com.joy.sandbox.feature.websocket.impl.presentation.addproducts.AddProductsViewModel
import com.joy.sandbox.feature.websocket.impl.presentation.products.WebSocketScreenProvider
import com.joy.sandbox.feature.websocket.impl.presentation.products.WebSocketViewModel
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap

@Module
class WebSocketModule {

    @Provides
    fun provideProductsInteractor(impl: ProductsInteractorImpl): ProductsInteractor = impl

    @Provides
    fun provideProductsRepository(impl: ProductsRepositoryImpl): ProductsRepository = impl

    @Provides
    @IntoMap
    @ViewModelKey(WebSocketViewModel::class)
    fun provideWebSocketViewModel(viewModel: WebSocketViewModel): ViewModel = viewModel

    @Provides
    @IntoMap
    @ViewModelKey(AddProductsViewModel::class)
    fun provideAddProductsViewModel(viewModel: AddProductsViewModel): ViewModel = viewModel

    @Provides
    @IntoMap
    @ScreenKey(WebSocketJoyScreen.ROUTE)
    fun provideWebSocketContent(): ScreenContentProvider = WebSocketScreenProvider()

    @Provides
    @IntoMap
    @ScreenKey(AddProductsJoyScreen.ROUTE)
    fun provideAddProductsContent(): ScreenContentProvider = AddProductsScreenProvider()
}

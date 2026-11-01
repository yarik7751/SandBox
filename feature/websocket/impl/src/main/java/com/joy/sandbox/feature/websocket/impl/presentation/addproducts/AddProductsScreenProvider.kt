package com.joy.sandbox.feature.websocket.impl.presentation.addproducts

import androidx.compose.runtime.Composable
import com.joy.sandbox.core.navigation.JoyScreen
import com.joy.sandbox.core.navigation.ScreenContentProvider
import com.joy.sandbox.core.navigation.router.JoyRouter
import com.joy.sandbox.core.ui.ViewModelFactory
import com.joy.sandbox.feature.websocket.api.AddProductsJoyScreen

class AddProductsScreenProvider : ScreenContentProvider {

    override val screen: JoyScreen<*> = AddProductsJoyScreen

    @Composable
    override fun Content(viewModelFactory: ViewModelFactory, router: JoyRouter) {
        AddProductsScreen(viewModelFactory = viewModelFactory, router = router)
    }
}

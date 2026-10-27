package com.joy.sandbox.feature.websocket.impl.presentation.products

import androidx.lifecycle.viewModelScope
import com.joy.sandbox.core.ui.BaseViewModel
import com.joy.sandbox.core.ui.CommandFlow
import com.joy.sandbox.feature.websocket.impl.domain.ProductsInteractor
import com.joy.sandbox.feature.websocket.impl.domain.model.Product
import com.joy.sandbox.utils.update
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class WebSocketViewModel @Inject constructor(
    private val productsInteractor: ProductsInteractor,
) : BaseViewModel() {

    val stateFlow = MutableStateFlow(State())
    val commandsFlow = CommandFlow<Commands>(viewModelScope)

    init {
        viewModelScope.launch {
            productsInteractor.observeProducts().collect { products ->
                stateFlow.update { it.copy(products = products) }
            }
        }
    }

    fun onAddClick() {
        commandsFlow.tryEmit(Commands.OpenAddProducts)
    }

    fun onRemoveClick(id: Long) {
        viewModelScope.launch {
            productsInteractor.removeProduct(id)
        }
    }

    data class State(
        val products: List<Product> = emptyList(),
    )

    sealed interface Commands {

        data object OpenAddProducts : Commands
    }
}

package com.joy.sandbox.feature.websocket.impl

import androidx.lifecycle.viewModelScope
import com.joy.sandbox.core.ui.BaseViewModel
import com.joy.sandbox.core.ui.CommandFlow
import com.joy.sandbox.feature.websocket.impl.domain.ProductsInteractor
import com.joy.sandbox.utils.update
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class AddProductsViewModel @Inject constructor(
    private val productsInteractor: ProductsInteractor,
) : BaseViewModel() {

    val stateFlow = MutableStateFlow(State())
    val commandsFlow = CommandFlow<Commands>(viewModelScope)

    init {
        viewModelScope.launch {
            productsInteractor.observeProducts().collect { products ->
                val addedSymbols = products.map { it.title }.toSet()
                val symbols = productsInteractor.getAvailableSymbols()
                stateFlow.update { state ->
                    state.copy(
                        items = symbols.map { symbol ->
                            SymbolItem(
                                symbol = symbol,
                                isAdded = symbol in addedSymbols,
                            )
                        },
                    )
                }
            }
        }
    }

    fun onAddClick(symbol: String) {
        viewModelScope.launch {
            productsInteractor.addProduct(symbol)
        }
    }

    data class State(
        val items: List<SymbolItem> = emptyList(),
    )

    data class SymbolItem(
        val symbol: String,
        val isAdded: Boolean,
    )

    sealed interface Commands
}

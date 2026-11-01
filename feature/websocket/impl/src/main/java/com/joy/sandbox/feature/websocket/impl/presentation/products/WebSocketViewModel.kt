package com.joy.sandbox.feature.websocket.impl.presentation.products

import androidx.lifecycle.viewModelScope
import com.joy.sandbox.core.ui.BaseViewModel
import com.joy.sandbox.core.ui.CommandFlow
import com.joy.sandbox.feature.websocket.impl.domain.ProductsInteractor
import com.joy.sandbox.feature.websocket.impl.domain.model.Price
import com.joy.sandbox.feature.websocket.impl.domain.model.PriceSnapshotParams
import com.joy.sandbox.feature.websocket.impl.domain.model.Product
import com.joy.sandbox.feature.websocket.impl.presentation.mapper.ProductUiModelMapper
import com.joy.sandbox.feature.websocket.impl.presentation.model.ProductUiModel
import com.joy.sandbox.utils.update
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class WebSocketViewModel @Inject constructor(
    private val productsInteractor: ProductsInteractor,
    private val productUiModelMapper: ProductUiModelMapper,
) : BaseViewModel() {

    val stateFlow = MutableStateFlow(State())
    val commandsFlow = CommandFlow<Commands>(viewModelScope)

    private val productsFlow = MutableStateFlow<List<Product>>(emptyList())
    private val pricesFlow = MutableStateFlow<Map<String, Price>>(emptyMap())

    init {
        productsInteractor.observeProducts()
            .onEach { products ->
                productsFlow.value = products
                productsInteractor.updatePriceSubscription(
                    PriceSnapshotParams(symbols = products.map { it.title }),
                )
                updateUiModels()
            }
            .launchIn(viewModelScope)
        productsInteractor.observePrices()
            .onEach { price ->
                pricesFlow.value += (price.symbol to price)
                updateUiModels()
            }
            .launchIn(viewModelScope)
    }

    override fun onCleared() {
        productsInteractor.disconnectPrices()
        super.onCleared()
    }

    fun onAddClick() {
        commandsFlow.tryEmit(Commands.OpenAddProducts)
    }

    fun onRemoveClick(id: Long) {
        if (stateFlow.value.products.size == 1) {
            stateFlow.update { it.copy(pendingRemovalId = id) }
        } else {
            removeProduct(id)
        }
    }

    fun onLastRemoveConfirmed() {
        val id = stateFlow.value.pendingRemovalId ?: return
        stateFlow.update { it.copy(pendingRemovalId = null) }
        removeProduct(id)
    }

    fun onLastRemoveDeclined() {
        stateFlow.update { it.copy(pendingRemovalId = null) }
    }

    private fun removeProduct(id: Long) {
        viewModelScope.launch {
            productsInteractor.removeProduct(id)
        }
    }

    private fun updateUiModels() {
        val prices = pricesFlow.value
        val uiModels = productsFlow.value
            .map { product -> productUiModelMapper.map(product, prices[product.title]) }
            .toPersistentList()
        stateFlow.update { it.copy(products = uiModels) }
    }

    data class State(
        val products: ImmutableList<ProductUiModel> = persistentListOf(),
        val pendingRemovalId: Long? = null,
    )

    sealed interface Commands {

        data object OpenAddProducts : Commands
    }
}

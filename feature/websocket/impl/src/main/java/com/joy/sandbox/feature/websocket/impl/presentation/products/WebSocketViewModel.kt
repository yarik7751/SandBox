package com.joy.sandbox.feature.websocket.impl.presentation.products

import androidx.lifecycle.viewModelScope
import com.joy.sandbox.core.ui.BaseViewModel
import com.joy.sandbox.core.ui.CommandFlow
import com.joy.sandbox.feature.websocket.impl.domain.ProductsInteractor
import com.joy.sandbox.feature.websocket.impl.presentation.mapper.ProductUiModelMapper
import com.joy.sandbox.feature.websocket.impl.presentation.model.ProductUiModel
import com.joy.sandbox.utils.update
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class WebSocketViewModel @Inject constructor(
    private val productsInteractor: ProductsInteractor,
    productUiModelMapper: ProductUiModelMapper,
) : BaseViewModel() {

    val stateFlow = MutableStateFlow(State())
    val commandsFlow = CommandFlow<Commands>(viewModelScope)

    init {
        viewModelScope.launch {
            productsInteractor.observeProducts()
                .map { products -> products.map(productUiModelMapper::map).toPersistentList() }
                .collect { products ->
                    stateFlow.update { it.copy(products = products) }
                }
        }
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

    data class State(
        val products: ImmutableList<ProductUiModel> = persistentListOf(),
        val pendingRemovalId: Long? = null,
    )

    sealed interface Commands {

        data object OpenAddProducts : Commands
    }
}

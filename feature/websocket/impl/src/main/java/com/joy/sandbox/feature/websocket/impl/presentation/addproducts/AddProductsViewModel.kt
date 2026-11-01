package com.joy.sandbox.feature.websocket.impl.presentation.addproducts

import androidx.lifecycle.viewModelScope
import com.joy.sandbox.core.ui.BaseViewModel
import com.joy.sandbox.core.ui.CommandFlow
import com.joy.sandbox.feature.websocket.impl.domain.ProductsInteractor
import com.joy.sandbox.utils.update
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

private const val MIN_QUERY_LENGTH = 2

class AddProductsViewModel @Inject constructor(
    private val productsInteractor: ProductsInteractor,
) : BaseViewModel() {

    val stateFlow = MutableStateFlow(State())
    val commandsFlow = CommandFlow<Commands>(viewModelScope)

    /** Полный список тикеров из источника данных. */
    private var symbols: List<String> = emptyList()

    /** Тикеры, уже добавленные в БД. */
    private var addedSymbols: Set<String> = emptySet()

    init {
        viewModelScope.launch {
            productsInteractor.observeProducts().collect { products ->
                addedSymbols = products.map { it.title }.toSet()
                symbols = productsInteractor.getAvailableSymbols().symbols
                updateItems()
            }
        }
    }

    fun onSearchClick() {
        stateFlow.update { it.copy(isSearchActive = true) }
    }

    fun onSearchClose() {
        stateFlow.update { it.copy(isSearchActive = false, searchQuery = "") }
        updateItems()
    }

    fun onSearchQueryChange(query: String) {
        stateFlow.update { it.copy(searchQuery = query) }
        updateItems()
    }

    fun onAddClick(symbol: String) {
        viewModelScope.launch {
            productsInteractor.addProduct(symbol)
        }
    }

    private fun updateItems() {
        val query = stateFlow.value.searchQuery
        val filtered = symbols.filterSymbols(query)
        stateFlow.update { state ->
            state.copy(
                items = filtered.map { symbol ->
                    SymbolItem(
                        symbol = symbol,
                        isAdded = symbol in addedSymbols,
                    )
                }.toPersistentList(),
                showEmptySearchResult = filtered.isEmpty() && query.length >= MIN_QUERY_LENGTH,
            )
        }
    }

    private fun List<String>.filterSymbols(query: String): List<String> {
        if (query.length < MIN_QUERY_LENGTH) return this
        return filter { it.contains(query, ignoreCase = true) }
    }

    data class State(
        val isSearchActive: Boolean = false,
        val searchQuery: String = "",
        val items: ImmutableList<SymbolItem> = persistentListOf(),
        val showEmptySearchResult: Boolean = false,
    )

    data class SymbolItem(
        val symbol: String,
        val isAdded: Boolean,
    )

    sealed interface Commands
}

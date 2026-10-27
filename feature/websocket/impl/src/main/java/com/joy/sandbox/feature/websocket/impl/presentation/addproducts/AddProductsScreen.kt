package com.joy.sandbox.feature.websocket.impl.presentation.addproducts

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.joy.sandbox.core.navigation.router.JoyRouter
import com.joy.sandbox.core.ui.SandBoxToolbar
import com.joy.sandbox.core.ui.SearchTextField
import com.joy.sandbox.core.ui.ViewModelFactory
import com.joy.sandbox.feature.websocket.impl.R

@Composable
fun AddProductsScreen(
    viewModelFactory: ViewModelFactory,
    router: JoyRouter,
) {
    val viewModel: AddProductsViewModel = viewModel(factory = viewModelFactory)
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            SandBoxToolbar(
                title = stringResource(R.string.add_products_title),
                onBackClick = {
                    if (state.isSearchActive) {
                        viewModel.onSearchClose()
                    } else {
                        router.exit()
                    }
                },
                actions = {
                    if (!state.isSearchActive) {
                        IconButton(onClick = viewModel::onSearchClick) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = stringResource(R.string.websocket_search),
                                tint = Color.Black,
                            )
                        }
                    }
                },
                titleContent = if (state.isSearchActive) {
                    {
                        SearchTextField(
                            modifier = Modifier.weight(1f),
                            query = state.searchQuery,
                            onQueryChange = viewModel::onSearchQueryChange,
                            placeholder = stringResource(R.string.websocket_search_hint),
                        )
                    }
                } else {
                    null
                },
            )
        },
    ) { padding ->
        if (state.showEmptySearchResult) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = stringResource(R.string.websocket_search_not_found),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                items(state.items, key = AddProductsViewModel.SymbolItem::symbol) { item ->
                    SymbolItemRow(
                        item = item,
                        onAddClick = { viewModel.onAddClick(item.symbol) },
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun SymbolItemRow(
    item: AddProductsViewModel.SymbolItem,
    onAddClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = item.symbol,
            style = MaterialTheme.typography.bodyLarge,
        )
        if (item.isAdded) {
            OutlinedButton(
                enabled = false,
                onClick = {},
            ) {
                Text(text = stringResource(R.string.add_products_added))
            }
        } else {
            Button(onClick = onAddClick) {
                Text(text = stringResource(R.string.add_products_add))
            }
        }
    }
}

package com.joy.sandbox.feature.websocket.impl.presentation.products

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.joy.sandbox.core.navigation.router.JoyRouter
import com.joy.sandbox.core.ui.SandBoxDialog
import com.joy.sandbox.core.ui.SandBoxToolbar
import com.joy.sandbox.core.ui.ViewModelFactory
import com.joy.sandbox.feature.websocket.api.AddProductsJoyScreen
import com.joy.sandbox.feature.websocket.impl.R
import com.joy.sandbox.feature.websocket.impl.presentation.products.WebSocketViewModel.Commands
import com.joy.sandbox.feature.websocket.impl.presentation.model.ProductUiModel
import com.joy.sandbox.utils.observe

@Composable
fun WebSocketScreen(
    viewModelFactory: ViewModelFactory,
    router: JoyRouter,
) {
    val viewModel: WebSocketViewModel = viewModel(factory = viewModelFactory)
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            SandBoxToolbar(
                title = stringResource(R.string.websocket_title),
                onBackClick = { router.exit() },
                actions = {
                    IconButton(onClick = viewModel::onAddClick) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = stringResource(R.string.websocket_add),
                            tint = androidx.compose.ui.graphics.Color.Black,
                        )
                    }
                },
            )
        },
    ) { padding ->
        if (state.products.isEmpty()) {
            EmptyProducts(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                onAddClick = viewModel::onAddClick,
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                items(state.products, key = ProductUiModel::id) { product ->
                    ProductItem(
                        product = product,
                        onRemoveClick = { viewModel.onRemoveClick(product.id) },
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                }
            }
        }
    }

    HandleCommands(viewModel = viewModel, router = router)

    state.pendingRemovalId?.let {
        SandBoxDialog(
            title = stringResource(R.string.websocket_remove_last_title),
            description = stringResource(R.string.websocket_remove_last_description),
            mainActionText = stringResource(R.string.websocket_remove_last_yes),
            onMainAction = viewModel::onLastRemoveConfirmed,
            neutralActionText = stringResource(R.string.websocket_remove_last_no),
            onNeutralAction = viewModel::onLastRemoveDeclined,
            onDismiss = viewModel::onLastRemoveDeclined,
        )
    }
}

@Composable
private fun ProductItem(
    product: ProductUiModel,
    onRemoveClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = product.title,
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            text = product.price,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        IconButton(onClick = onRemoveClick) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = stringResource(R.string.websocket_remove),
                tint = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun EmptyProducts(
    modifier: Modifier = Modifier,
    onAddClick: () -> Unit,
) {
    Box(modifier = modifier) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.websocket_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
                modifier = Modifier.padding(top = 16.dp),
                onClick = onAddClick,
            ) {
                Text(text = stringResource(R.string.websocket_add_button))
            }
        }
    }
}

@Composable
private fun HandleCommands(
    viewModel: WebSocketViewModel,
    router: JoyRouter,
) {
    viewModel.commandsFlow.observe { command ->
        when (command) {
            Commands.OpenAddProducts -> router.navigateTo(AddProductsJoyScreen)
        }
    }
}

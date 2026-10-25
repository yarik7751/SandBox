package com.joy.sandbox.feature.websocket.impl

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.joy.sandbox.core.navigation.router.JoyRouter
import com.joy.sandbox.core.ui.SandBoxToolbar
import com.joy.sandbox.core.ui.ViewModelFactory
import com.joy.sandbox.feature.websocket.impl.WebSocketViewModel.ConnectionStatus

@Composable
fun WebSocketScreen(
    viewModelFactory: ViewModelFactory,
    router: JoyRouter,
) {
    val viewModel: WebSocketViewModel = viewModel(factory = viewModelFactory)
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            SandBoxToolbar(
                title = stringResource(R.string.websocket_title),
                onBackClick = { router.exit() },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = stringResource(statusTextRes(state.status)),
                color = statusColor(state.status),
                style = MaterialTheme.typography.titleMedium,
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextField(
                    modifier = Modifier.weight(1f),
                    value = state.serverUrl,
                    onValueChange = viewModel::onServerUrlChange,
                    enabled = state.status == ConnectionStatus.Disconnected,
                    singleLine = true,
                    placeholder = {
                        Text(text = stringResource(R.string.websocket_url_hint))
                    },
                )
                Button(
                    modifier = Modifier.padding(start = 8.dp),
                    onClick = {
                        when (state.status) {
                            ConnectionStatus.Disconnected -> viewModel.connect()
                            ConnectionStatus.Connecting, ConnectionStatus.Connected ->
                                viewModel.disconnect()
                        }
                    },
                ) {
                    Text(
                        text = stringResource(
                            when (state.status) {
                                ConnectionStatus.Disconnected -> R.string.websocket_connect
                                ConnectionStatus.Connecting, ConnectionStatus.Connected ->
                                    R.string.websocket_disconnect
                            }
                        )
                    )
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 8.dp),
            ) {
                items(state.messages) { message ->
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        text = (if (message.isOutgoing) "→ " else "← ") + message.text,
                        textAlign = if (message.isOutgoing) TextAlign.End else TextAlign.Start,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextField(
                    modifier = Modifier.weight(1f),
                    value = state.input,
                    onValueChange = viewModel::onInputChange,
                    enabled = state.status == ConnectionStatus.Connected,
                    placeholder = {
                        Text(text = stringResource(R.string.websocket_message_hint))
                    },
                )
                Button(
                    modifier = Modifier.padding(start = 8.dp),
                    enabled = state.status == ConnectionStatus.Connected,
                    onClick = viewModel::send,
                ) {
                    Text(text = stringResource(R.string.websocket_send))
                }
            }
        }
    }
}

private fun statusTextRes(status: ConnectionStatus): Int {
    return when (status) {
        ConnectionStatus.Disconnected -> R.string.websocket_status_disconnected
        ConnectionStatus.Connecting -> R.string.websocket_status_connecting
        ConnectionStatus.Connected -> R.string.websocket_status_connected
    }
}

@Composable
private fun statusColor(status: ConnectionStatus): Color {
    return when (status) {
        ConnectionStatus.Disconnected -> MaterialTheme.colorScheme.error
        ConnectionStatus.Connecting -> MaterialTheme.colorScheme.tertiary
        ConnectionStatus.Connected -> MaterialTheme.colorScheme.primary
    }
}

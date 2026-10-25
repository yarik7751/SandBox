package com.joy.sandbox.feature.websocket.impl

import androidx.lifecycle.viewModelScope
import com.joy.sandbox.core.ui.BaseViewModel
import com.joy.sandbox.core.ui.CommandFlow
import com.joy.sandbox.core.ui.snackbar.flow.SnackBarManager
import com.joy.sandbox.core.ui.snackbar.model.SnackBarData
import com.joy.sandbox.core.ui.snackbar.model.SnackBarType
import com.joy.sandbox.utils.textorresource.TextOrResource
import com.joy.sandbox.utils.update
import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import javax.inject.Inject

private const val NORMAL_CLOSURE_STATUS = 1000
private const val MAX_MESSAGES = 100

class WebSocketViewModel @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val snackBarManager: SnackBarManager,
) : BaseViewModel() {

    val stateFlow = MutableStateFlow(State())
    val commandsFlow = CommandFlow<Commands>(viewModelScope)

    private var webSocket: WebSocket? = null

    private val webSocketListener = object : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            stateFlow.update { it.copy(status = ConnectionStatus.Connected) }
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            addMessage(ChatMessage(text = text, isOutgoing = false))
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            addMessage(ChatMessage(text = bytes.hex(), isOutgoing = false))
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            webSocket.close(code, reason)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            handleClosed()
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            handleClosed()
            showError(t.message ?: "Connection failure")
        }
    }

    fun onServerUrlChange(url: String) {
        stateFlow.update { it.copy(serverUrl = url) }
    }

    fun onInputChange(text: String) {
        stateFlow.update { it.copy(input = text) }
    }

    fun connect() {
        val url = stateFlow.value.serverUrl.trim()
        if (url.isEmpty()) {
            showError("Server URL is empty")
            return
        }
        if (webSocket != null) return

        stateFlow.update { it.copy(status = ConnectionStatus.Connecting) }
        try {
            val request = Request.Builder().url(url).build()
            webSocket = okHttpClient.newWebSocket(request, webSocketListener)
        } catch (e: IllegalArgumentException) {
            handleClosed()
            showError(e.message ?: "Invalid server URL")
        }
    }

    fun disconnect() {
        val socket = webSocket ?: return
        stateFlow.update { it.copy(status = ConnectionStatus.Disconnected) }
        socket.close(NORMAL_CLOSURE_STATUS, null)
        webSocket = null
    }

    fun send() {
        val text = stateFlow.value.input.trim()
        if (text.isEmpty()) return

        val sent = webSocket?.send(text) ?: false
        if (sent) {
            addMessage(ChatMessage(text = text, isOutgoing = true))
            stateFlow.update { it.copy(input = "") }
        } else {
            showError("Not connected")
        }
    }

    private fun addMessage(message: ChatMessage) {
        stateFlow.update { state ->
            state.copy(messages = (state.messages + message).takeLast(MAX_MESSAGES))
        }
    }

    private fun handleClosed() {
        webSocket = null
        stateFlow.update { it.copy(status = ConnectionStatus.Disconnected) }
    }

    private fun showError(message: String) {
        snackBarManager.showMessage(
            SnackBarData(
                type = SnackBarType.Error,
                message = TextOrResource.Text(message),
            )
        )
    }

    override fun onCleared() {
        webSocket?.close(NORMAL_CLOSURE_STATUS, null)
        webSocket = null
    }

    data class State(
        val serverUrl: String = "",
        val status: ConnectionStatus = ConnectionStatus.Disconnected,
        val input: String = "",
        val messages: List<ChatMessage> = emptyList(),
    )

    enum class ConnectionStatus {
        Disconnected, Connecting, Connected,
    }

    data class ChatMessage(
        val text: String,
        val isOutgoing: Boolean,
    )

    sealed interface Commands
}

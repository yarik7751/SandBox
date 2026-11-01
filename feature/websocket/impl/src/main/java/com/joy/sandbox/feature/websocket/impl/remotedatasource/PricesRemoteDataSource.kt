package com.joy.sandbox.feature.websocket.impl.remotedatasource

import android.util.Log
import com.joy.sandbox.feature.websocket.impl.di.qualifier.WebSocketOkHttpClient
import com.joy.sandbox.feature.websocket.impl.remotedatasource.mapper.mapToPriceDto
import com.joy.sandbox.feature.websocket.impl.remotedatasource.model.PriceDto
import com.joy.sandbox.feature.websocket.impl.remotedatasource.proto.Yaticker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import okio.ByteString.Companion.decodeBase64
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.min
import kotlin.math.pow

class PricesRemoteDataSource @Inject constructor(
    @WebSocketOkHttpClient private val webSocketOkHttpClient: OkHttpClient,
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val pricesMutableFlow = MutableSharedFlow<PriceDto>(extraBufferCapacity = PRICES_BUFFER)
    val pricesFlow: Flow<PriceDto> = pricesMutableFlow.asSharedFlow()

    private var webSocket: WebSocket? = null
    private var symbols: List<String> = emptyList()
    private var manualDisconnect = false
    private var reconnectAttempts = 0
    private var keepAliveJob: Job? = null
    private var reconnectJob: Job? = null

    private val listener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            reconnectAttempts = 0
            sendSubscribe(symbols)
            startKeepAlive()
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            handleTextMessage(text)
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            handleBinaryMessage(bytes)
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            // OkHttp сам отвечает на close-фрейм и завершит соединение.
            Log.d(TAG, "onClosing: code=$code, reason=$reason")
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(TAG, "onClosed: code=$code, reason=$reason")
            scheduleReconnect()
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e(TAG, "onFailure: ${t.message}", t)
            scheduleReconnect()
        }
    }

    fun connect(newSymbols: List<String>) {
        if (newSymbols.isEmpty()) return
        symbols = newSymbols.distinct()
        manualDisconnect = false
        reconnectAttempts = 0
        openSocket()
    }

    fun updateSymbols(newSymbols: List<String>) {
        val oldSet = symbols.toSet()
        val newSet = newSymbols.distinct()
        symbols = newSet
        if (webSocket == null || manualDisconnect) return

        val toUnsubscribe = oldSet - newSet.toSet()
        val toSubscribe = newSet.toSet() - oldSet
        if (toUnsubscribe.isNotEmpty()) {
            webSocket?.send(createSubscribeJson(JSON_KEY_UNSUBSCRIBE, toUnsubscribe))
        }
        if (toSubscribe.isNotEmpty()) {
            webSocket?.send(createSubscribeJson(JSON_KEY_SUBSCRIBE, toSubscribe))
        }
    }

    fun disconnect() {
        manualDisconnect = true
        keepAliveJob?.cancel()
        reconnectJob?.cancel()
        reconnectAttempts = 0
        webSocket?.close(CLOSE_CODE_NORMAL, null)
        webSocket = null
    }

    private fun openSocket() {
        keepAliveJob?.cancel()
        reconnectJob?.cancel()
        webSocket?.close(CLOSE_CODE_NORMAL, null)
        webSocket = webSocketOkHttpClient.newWebSocket(
            Request.Builder().url(WEBSOCKET_URL).build(),
            listener,
        )
    }

    private fun startKeepAlive() {
        keepAliveJob?.cancel()
        keepAliveJob = scope.launch {
            while (isActive) {
                delay(KEEP_ALIVE_INTERVAL_MILLIS)
                sendSubscribe(symbols)
            }
        }
    }

    private fun sendSubscribe(symbolsToSubscribe: List<String>) {
        if (symbolsToSubscribe.isEmpty()) return
        webSocket?.send(createSubscribeJson(JSON_KEY_SUBSCRIBE, symbolsToSubscribe))
    }

    private fun createSubscribeJson(key: String, targetSymbols: Collection<String>): String {
        return JSONObject()
            .put(key, targetSymbols.fold(org.json.JSONArray()) { array, symbol -> array.put(symbol) })
            .toString()
    }

    private fun scheduleReconnect() {
        if (manualDisconnect || symbols.isEmpty()) return
        keepAliveJob?.cancel()
        reconnectJob?.cancel()
        val delaySeconds = min(
            RECONNECT_BASE_DELAY_SECONDS * 2.0.pow(reconnectAttempts),
            RECONNECT_MAX_DELAY_SECONDS,
        )
        reconnectAttempts++
        Log.d(TAG, "Reconnect in ${delaySeconds.toInt()} s, attempt=$reconnectAttempts")
        reconnectJob = scope.launch {
            delay(TimeUnit.SECONDS.toMillis(delaySeconds.toLong()))
            openSocket()
        }
    }

    private fun handleTextMessage(text: String) {
        // Вариант A: JSON-обертка {"type":"pricing","message":"<base64 protobuf>"}.
        val base64Payload = runCatching { JSONObject(text).optString(JSON_KEY_MESSAGE) }
            .getOrNull()
            ?.takeIf { it.isNotBlank() }
            ?: return handleBase64Fallback(text)

        val bytes = base64Payload.decodeBase64()
        if (bytes == null) {
            Log.w(TAG, "Can't decode base64 payload")
            return
        }
        parseTicker(bytes)?.let { emitTicker(it) }
    }

    private fun handleBase64Fallback(text: String) {
        // Вариант C: "голый" base64 без JSON-обертки.
        val ticker = text.decodeBase64()?.let { parseTicker(it) } ?: run {
            // Вариант D: не-JSON и не-base64 — игнорируем.
            Log.w(TAG, "Unexpected message: $text")
            return
        }
        emitTicker(ticker)
    }

    private fun handleBinaryMessage(bytes: ByteString) {
        // Вариант C: бинарный фрейм с protobuf.
        parseTicker(bytes)?.let { emitTicker(it) }
    }

    private fun parseTicker(bytes: ByteString): Yaticker? {
        return runCatching { Yaticker.parseFrom(bytes.toByteArray()) }
            .onFailure { Log.w(TAG, "Can't parse Yaticker: ${it.message}") }
            .getOrNull()
    }

    private fun emitTicker(ticker: Yaticker) {
        // Вариант B: heartbeat сервера — наружу не эмитим.
        if (ticker.quoteType == Yaticker.QuoteType.HEARTBEAT) return
        reconnectAttempts = 0
        pricesMutableFlow.tryEmit(ticker.mapToPriceDto())
    }

    private companion object {
        const val TAG = "PricesRemoteDataSource"
        const val WEBSOCKET_URL = "wss://streamer.finance.yahoo.com/?version=2"
        const val JSON_KEY_SUBSCRIBE = "subscribe"
        const val JSON_KEY_UNSUBSCRIBE = "unsubscribe"
        const val JSON_KEY_MESSAGE = "message"
        const val CLOSE_CODE_NORMAL = 1000
        const val PRICES_BUFFER = 64
        const val KEEP_ALIVE_INTERVAL_MILLIS = 15_000L
        const val RECONNECT_BASE_DELAY_SECONDS = 2.0
        const val RECONNECT_MAX_DELAY_SECONDS = 120.0
    }
}

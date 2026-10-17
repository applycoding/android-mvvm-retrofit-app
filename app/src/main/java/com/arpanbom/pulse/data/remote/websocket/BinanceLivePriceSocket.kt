package com.arpanbom.pulse.data.remote.websocket

import com.arpanbom.pulse.di.BinanceWsBaseUrl
import com.arpanbom.pulse.di.WsClient
import com.arpanbom.pulse.domain.model.LiveQuote
import com.arpanbom.pulse.domain.repository.LivePriceStream
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.retryWhen
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

data class RetryPolicy(
    val maxAttempts: Int = 5,
    val initialDelayMs: Long = 1_000,
    val maxDelayMs: Long = 15_000,
)

class BinanceLivePriceSocket @Inject constructor(
    @WsClient private val client: OkHttpClient,
    @BinanceWsBaseUrl private val baseUrl: String,
    private val parser: MiniTickerParser,
    private val retryPolicy: RetryPolicy,
) : LivePriceStream {
    override fun observe(symbols: Set<String>): Flow<LiveQuote> {
        if (symbols.isEmpty()) return emptyFlow()
        val url = binanceStreamUrl(baseUrl, symbols)
        return callbackFlow {
            val request = Request.Builder().url(url).build()
            val socket = client.newWebSocket(
                request,
                object : WebSocketListener() {
                    override fun onMessage(webSocket: WebSocket, text: String) {
                        parser.parse(text)?.let { quote -> trySend(quote) }
                    }

                    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                        close(t)
                    }

                    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                        close()
                    }
                },
            )
            awaitClose { socket.close(NORMAL_CLOSURE, "disposed") }
        }.retryWhen { _, attempt ->
            if (attempt >= retryPolicy.maxAttempts) {
                false
            } else {
                delay(retryDelayMillis(attempt, retryPolicy))
                true
            }
        }
    }

    private companion object {
        const val NORMAL_CLOSURE = 1000
    }
}

internal fun binanceStreamUrl(baseUrl: String, symbols: Set<String>): String {
    val streams = symbols
        .map { it.uppercase().removeSuffix("USDT") }
        .filter { it.isNotBlank() }
        .distinct()
        .sorted()
        .joinToString("/") { symbol -> "${symbol.lowercase()}usdt@miniTicker" }
    return baseUrl.trimEnd('/') + "/stream?streams=" + streams
}

internal fun retryDelayMillis(attempt: Long, policy: RetryPolicy): Long {
    val multiplier = 1L shl attempt.coerceIn(0, 10).toInt()
    return (policy.initialDelayMs * multiplier).coerceAtMost(policy.maxDelayMs)
}

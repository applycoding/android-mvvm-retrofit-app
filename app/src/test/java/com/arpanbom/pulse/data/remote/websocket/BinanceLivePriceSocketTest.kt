package com.arpanbom.pulse.data.remote.websocket

import app.cash.turbine.test
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Test

class BinanceLivePriceSocketTest {
    @Test
    fun buildsSortedCombinedStreamUrl() {
        assertEquals(
            "wss://stream.example/stream?streams=btcusdt@miniTicker/ethusdt@miniTicker",
            binanceStreamUrl("wss://stream.example/", setOf("ETH", "btc")),
        )
    }

    @Test
    fun retryDelayGrowsAndCaps() {
        val policy = RetryPolicy(initialDelayMs = 100, maxDelayMs = 400, maxAttempts = 5)
        assertEquals(100L, retryDelayMillis(0, policy))
        assertEquals(200L, retryDelayMillis(1, policy))
        assertEquals(400L, retryDelayMillis(2, policy))
        assertEquals(400L, retryDelayMillis(8, policy))
    }

    @Test
    fun emitsQuoteFromSocket() = runBlocking {
        val server = MockWebServer()
        val client = OkHttpClient()
        server.start()
        try {
            server.enqueue(
                MockResponse().withWebSocketUpgrade(
                    object : WebSocketListener() {
                        override fun onOpen(webSocket: WebSocket, response: Response) {
                            webSocket.send(
                                """{"stream":"btcusdt@miniTicker","data":{"e":"24hrMiniTicker","E":1710000000000,"s":"BTCUSDT","c":"65000.5"}}""",
                            )
                        }
                    },
                ),
            )
            val socket = socket(client, server, RetryPolicy(maxAttempts = 0))
            socket.observe(setOf("BTC")).test(timeout = 10.seconds) {
                val quote = awaitItem()
                assertEquals("BTC", quote.symbol)
                assertEquals(65000.5, quote.price, 0.001)
                assertEquals(1710000000000L, quote.eventTimeEpochMs)
                cancelAndIgnoreRemainingEvents()
            }
        } finally {
            client.dispatcher.executorService.shutdown()
            client.connectionPool.evictAll()
            server.shutdown()
        }
    }

    @Test
    fun failedUpgradeEndsTheStreamWhenRetriesAreDisabled() = runBlocking {
        val server = MockWebServer()
        val client = OkHttpClient()
        server.start()
        try {
            server.enqueue(MockResponse().setResponseCode(500))
            val socket = socket(client, server, RetryPolicy(maxAttempts = 0))
            socket.observe(setOf("BTC")).test(timeout = 10.seconds) {
                awaitError()
            }
        } finally {
            client.dispatcher.executorService.shutdown()
            client.connectionPool.evictAll()
            server.shutdown()
        }
    }

    private fun socket(client: OkHttpClient, server: MockWebServer, policy: RetryPolicy): BinanceLivePriceSocket {
        val baseUrl = server.url("/").toString().replaceFirst("http://", "ws://")
        return BinanceLivePriceSocket(client, baseUrl, MiniTickerParser(), policy)
    }
}

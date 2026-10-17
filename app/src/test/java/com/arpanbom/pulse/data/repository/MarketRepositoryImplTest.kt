package com.arpanbom.pulse.data.repository

import app.cash.turbine.test
import com.arpanbom.pulse.data.local.SampleMarkets
import com.arpanbom.pulse.data.local.dao.MarketDao
import com.arpanbom.pulse.data.local.entity.MarketEntity
import com.arpanbom.pulse.data.remote.CoinGeckoApi
import com.arpanbom.pulse.domain.model.LiveQuote
import com.arpanbom.pulse.domain.model.MarketError
import com.arpanbom.pulse.domain.model.RefreshResult
import com.arpanbom.pulse.domain.repository.LivePriceStream
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import io.mockk.any
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.withArg
import java.time.Instant
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Retrofit

@OptIn(ExperimentalCoroutinesApi::class)
class MarketRepositoryImplTest {
    private val dao = mockk<MarketDao>()
    private val server = MockWebServer()
    private val json = Json { ignoreUnknownKeys = true }
    private val client = OkHttpClient.Builder()
        .connectTimeout(2, TimeUnit.SECONDS)
        .readTimeout(2, TimeUnit.SECONDS)
        .build()

    @After
    fun tearDown() {
        client.dispatcher.executorService.shutdown()
        client.connectionPool.evictAll()
    }

    @Test
    fun refreshPersistsParsedMarkets() = runTest {
        server.start()
        try {
            server.enqueue(jsonResponse(fixture("markets.json")))
            coEvery { dao.replaceAll(any()) } returns Unit
            val result = repository().refresh()
            assertEquals(RefreshResult.Success, result)
            coVerify {
                dao.replaceAll(
                    withArg { rows ->
                        assertEquals(listOf("bitcoin", "ethereum"), rows.map(MarketEntity::id))
                        assertEquals("btc", rows.first().symbol)
                        assertEquals(65000.12, rows.first().currentPrice, 0.001)
                        assertEquals(
                            Instant.parse("2026-10-16T12:00:00.000Z").toEpochMilli(),
                            rows.first().lastUpdatedEpochMs,
                        )
                    },
                )
            }
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun rateLimitWithEmptyCacheSeedsSampleMarkets() = runTest {
        server.start()
        try {
            server.enqueue(MockResponse().setResponseCode(429).setBody("""{"error":"slow down"}"""))
            coEvery { dao.count() } returns 0
            coEvery { dao.upsertAll(any()) } returns Unit
            val result = repository().refresh()
            val failure = result as RefreshResult.Failed
            assertEquals(MarketError.RateLimited, failure.error)
            assertTrue(failure.usedSampleData)
            coVerify {
                dao.upsertAll(
                    withArg { rows ->
                        assertEquals(SampleMarkets.entities().map(MarketEntity::id), rows.map(MarketEntity::id))
                    },
                )
            }
            coVerify(exactly = 0) { dao.replaceAll(any()) }
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun serverErrorKeepsExistingCache() = runTest {
        server.start()
        try {
            server.enqueue(MockResponse().setResponseCode(503))
            coEvery { dao.count() } returns 4
            val result = repository().refresh()
            val failure = result as RefreshResult.Failed
            assertEquals(MarketError.Server, failure.error)
            assertFalse(failure.usedSampleData)
            coVerify(exactly = 0) { dao.upsertAll(any()) }
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun malformedPayloadIsUnknownAndCanSeedSamples() = runTest {
        server.start()
        try {
            server.enqueue(jsonResponse("{"))
            coEvery { dao.count() } returns 0
            coEvery { dao.upsertAll(any()) } returns Unit
            val result = repository().refresh() as RefreshResult.Failed
            assertEquals(MarketError.Unknown, result.error)
            assertTrue(result.usedSampleData)
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun offlineFailureDoesNotPretendTheRequestSucceeded() = runTest {
        val port = unusedPort()
        coEvery { dao.count() } returns 2
        val result = repository(baseUrl = "http://127.0.0.1:$port/").refresh()
        val failure = result as RefreshResult.Failed
        assertEquals(MarketError.Offline, failure.error)
        assertFalse(failure.usedSampleData)
    }

    @Test
    fun liveQuotesDropAssetsBinanceDoesNotList() = runTest {
        val stream = RecordingStream()
        val repo = repository(stream = stream, baseUrl = "http://127.0.0.1:9/")
        repo.observeLiveQuotes(setOf("btc", "notacoin", "eth")).test {
            awaitComplete()
        }
        assertEquals(setOf("BTC", "ETH"), stream.requested)
    }

    private fun repository(
        stream: LivePriceStream = RecordingStream(),
        baseUrl: String = server.url("/").toString(),
    ): MarketRepositoryImpl {
        val api = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(CoinGeckoApi::class.java)
        return MarketRepositoryImpl(
            api = api,
            dao = dao,
            livePriceStream = stream,
            ioDispatcher = UnconfinedTestDispatcher(),
        )
    }

    private fun jsonResponse(body: String): MockResponse =
        MockResponse()
            .setHeader("Content-Type", "application/json")
            .setBody(body)

    private fun fixture(name: String): String =
        checkNotNull(javaClass.classLoader).getResource(name)!!.readText()

    private fun unusedPort(): Int {
        val parked = MockWebServer()
        parked.start()
        val port = parked.port
        parked.shutdown()
        return port
    }

    private class RecordingStream : LivePriceStream {
        var requested: Set<String> = emptySet()

        override fun observe(symbols: Set<String>): Flow<LiveQuote> {
            requested = symbols
            return emptyFlow()
        }
    }
}

package com.arpanbom.pulse.ui.markets

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.arpanbom.pulse.domain.model.LiveQuote
import com.arpanbom.pulse.domain.model.MarketCoin
import com.arpanbom.pulse.domain.model.MarketError
import com.arpanbom.pulse.domain.model.RefreshResult
import com.arpanbom.pulse.domain.repository.NetworkStatus
import com.arpanbom.pulse.domain.usecase.ObserveLiveQuotesUseCase
import com.arpanbom.pulse.domain.usecase.ObserveMarketsUseCase
import com.arpanbom.pulse.domain.usecase.RefreshMarketsUseCase
import com.arpanbom.pulse.testutil.MainDispatcherRule
import io.mockk.coAnswers
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MarketsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val markets = MutableStateFlow<List<MarketCoin>>(emptyList())
    private val observeMarkets = mockk<ObserveMarketsUseCase>()
    private val refreshMarkets = mockk<RefreshMarketsUseCase>()
    private val observeLiveQuotes = mockk<ObserveLiveQuotesUseCase>()
    private val networkStatus = mockk<NetworkStatus>()

    @Test
    fun refreshSuccessExposesCachedCoins() = runTest {
        stub(online = true)
        coEvery { refreshMarkets() } coAnswers {
            markets.value = listOf(bitcoin(), ethereum())
            RefreshResult.Success
        }
        val viewModel = viewModel()

        viewModel.uiState.test(timeout = 5.seconds) {
            assertFalse(awaitItem().hasLoaded)
            viewModel.refresh()
            val loaded = awaitSettled()
            assertTrue(loaded.hasLoaded)
            assertFalse(loaded.isRefreshing)
            assertNull(loaded.error)
            assertFalse(loaded.usedSampleData)
            assertEquals(listOf("bitcoin", "ethereum"), loaded.coins.map { it.coin.id })
            assertEquals(64_250.0, loaded.coins.first().displayPrice, 0.001)
            assertFalse(loaded.isLive)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun queryFiltersByNameOrSymbol() = runTest {
        markets.value = listOf(bitcoin(), ethereum())
        stub(online = true)
        coEvery { refreshMarkets() } returns RefreshResult.Success
        val viewModel = viewModel()

        viewModel.uiState.test(timeout = 5.seconds) {
            awaitItem()
            viewModel.refresh()
            val loaded = awaitSettled()
            assertEquals(2, loaded.coins.size)
            viewModel.onQueryChange("eth")
            val filtered = awaitItem()
            assertEquals(listOf("ethereum"), filtered.coins.map { it.coin.id })
            assertEquals("eth", filtered.query)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun liveQuoteReplacesDisplayPrice() = runTest {
        markets.value = listOf(bitcoin())
        stub(online = true, quotes = mapOf("BTC" to LiveQuote("BTC", 70_000.0, 5L)))
        coEvery { refreshMarkets() } returns RefreshResult.Success
        val viewModel = viewModel()

        viewModel.uiState.test(timeout = 5.seconds) {
            awaitItem()
            viewModel.refresh()
            var latest = awaitSettled()
            while (!latest.coins.single().isLive) {
                latest = awaitItem()
            }
            assertEquals(70_000.0, latest.coins.single().displayPrice, 0.001)
            assertTrue(latest.isLive)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun failedRefreshKeepsRowsAndRecordsTheError() = runTest {
        markets.value = listOf(bitcoin())
        stub(online = false)
        coEvery { refreshMarkets() } returns RefreshResult.Failed(
            error = MarketError.Offline,
            usedSampleData = true,
        )
        val viewModel = viewModel()

        viewModel.uiState.test(timeout = 5.seconds) {
            awaitItem()
            viewModel.refresh()
            val failed = awaitSettled()
            assertEquals(MarketError.Offline, failed.error)
            assertTrue(failed.usedSampleData)
            assertFalse(failed.isOnline)
            assertEquals("bitcoin", failed.coins.single().coin.id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun stub(online: Boolean, quotes: Map<String, LiveQuote> = emptyMap()) {
        every { observeMarkets() } returns markets
        every { observeLiveQuotes(any()) } returns flowOf(quotes)
        every { networkStatus.isOnline() } returns flowOf(online)
    }

    private fun viewModel(): MarketsViewModel = MarketsViewModel(
        observeMarkets = observeMarkets,
        refreshMarkets = refreshMarkets,
        observeLiveQuotes = observeLiveQuotes,
        networkStatus = networkStatus,
    )

    private suspend fun ReceiveTurbine<MarketsUiState>.awaitSettled(): MarketsUiState {
        var latest = awaitItem()
        if (!latest.hasLoaded || latest.isRefreshing) {
            latest = awaitItem()
        }
        return latest
    }
}

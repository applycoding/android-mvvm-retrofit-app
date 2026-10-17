package com.arpanbom.pulse.ui.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.arpanbom.pulse.domain.model.LiveQuote
import com.arpanbom.pulse.domain.model.MarketCoin
import com.arpanbom.pulse.domain.usecase.ObserveCoinUseCase
import com.arpanbom.pulse.domain.usecase.ObserveLiveQuotesUseCase
import com.arpanbom.pulse.testutil.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CoinDetailViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val observeCoin = mockk<ObserveCoinUseCase>()
    private val observeLiveQuotes = mockk<ObserveLiveQuotesUseCase>()

    @Test
    fun showsCoinAndLivePrice() = runTest {
        every { observeCoin("bitcoin") } returns flowOf(bitcoin())
        every { observeLiveQuotes(any()) } returns flowOf(mapOf("BTC" to LiveQuote("BTC", 70_000.0, 1L)))
        val viewModel = viewModel()

        viewModel.uiState.test(timeout = 5.seconds) {
            val state = awaitItem().let { first ->
                if (first.coin == null) awaitItem() else first
            }
            assertEquals("bitcoin", state.coin?.id)
            assertEquals(70_000.0, state.displayPrice ?: 0.0, 0.001)
            assertTrue(state.isLive)
            assertFalse(state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun missingCoinLeavesTheDetailEmpty() = runTest {
        every { observeCoin("bitcoin") } returns flowOf(null)
        every { observeLiveQuotes(any()) } returns flowOf(emptyMap())
        val viewModel = viewModel()

        viewModel.uiState.test(timeout = 5.seconds) {
            val state = awaitItem().let { first ->
                if (first.isLoading) awaitItem() else first
            }
            assertFalse(state.isLoading)
            assertNull(state.coin)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun viewModel(): CoinDetailViewModel = CoinDetailViewModel(
        savedStateHandle = SavedStateHandle(mapOf(CoinDetailViewModel.COIN_ID to "bitcoin")),
        observeCoin = observeCoin,
        observeLiveQuotes = observeLiveQuotes,
    )

    private fun bitcoin(): MarketCoin = MarketCoin(
        id = "bitcoin",
        symbol = "btc",
        name = "Bitcoin",
        imageUrl = "",
        currentPrice = 64_250.0,
        priceChangePercent24h = 1.8,
        marketCap = 1_270_000_000_000.0,
        volume24h = 20_000_000_000.0,
        high24h = 65_000.0,
        low24h = 63_000.0,
        rank = 1,
        lastUpdatedEpochMs = 0L,
    )
}

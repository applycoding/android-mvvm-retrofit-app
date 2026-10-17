package com.arpanbom.pulse.ui.markets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arpanbom.pulse.domain.model.LiveQuote
import com.arpanbom.pulse.domain.model.MarketCoin
import com.arpanbom.pulse.domain.model.MarketError
import com.arpanbom.pulse.domain.model.RefreshResult
import com.arpanbom.pulse.domain.repository.NetworkStatus
import com.arpanbom.pulse.domain.usecase.ObserveLiveQuotesUseCase
import com.arpanbom.pulse.domain.usecase.ObserveMarketsUseCase
import com.arpanbom.pulse.domain.usecase.RefreshMarketsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class MarketsViewModel @Inject constructor(
    observeMarkets: ObserveMarketsUseCase,
    private val refreshMarkets: RefreshMarketsUseCase,
    observeLiveQuotes: ObserveLiveQuotesUseCase,
    networkStatus: NetworkStatus,
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val snapshot = MutableStateFlow(LoadSnapshot())

    private val quotes: StateFlow<Map<String, LiveQuote>> = observeMarkets()
        .map { coins -> coins.take(LIVE_SYMBOL_LIMIT).map { it.symbol.uppercase() }.toSet() }
        .distinctUntilChanged()
        .flatMapLatest { symbols -> observeLiveQuotes(symbols) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyMap())

    val uiState: StateFlow<MarketsUiState> = combine(
        observeMarkets(),
        query,
        snapshot,
        quotes,
        networkStatus.isOnline(),
    ) { coins, currentQuery, load, liveQuotes, online ->
        val rows = coins.toRows(liveQuotes, currentQuery)
        MarketsUiState(
            coins = rows,
            query = currentQuery,
            isRefreshing = load.isRefreshing,
            hasLoaded = load.hasLoaded,
            isOnline = online,
            error = load.error,
            usedSampleData = load.usedSampleData,
            isLive = rows.any { it.isLive },
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        MarketsUiState(),
    )

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun refresh() {
        viewModelScope.launch {
            snapshot.update { it.copy(isRefreshing = true, error = null) }
            when (val result = refreshMarkets()) {
                RefreshResult.Success -> snapshot.update {
                    it.copy(
                        isRefreshing = false,
                        error = null,
                        usedSampleData = false,
                        hasLoaded = true,
                    )
                }
                is RefreshResult.Failed -> snapshot.update {
                    it.copy(
                        isRefreshing = false,
                        error = result.error,
                        usedSampleData = result.usedSampleData,
                        hasLoaded = true,
                    )
                }
                else -> {
                    val unexpected: Nothing = result
                    unexpected
                }
            }
        }
    }

    private data class LoadSnapshot(
        val isRefreshing: Boolean = false,
        val hasLoaded: Boolean = false,
        val error: MarketError? = null,
        val usedSampleData: Boolean = false,
    )

    private companion object {
        const val LIVE_SYMBOL_LIMIT = 20
        const val STOP_TIMEOUT_MS = 5_000L
    }
}

private fun List<MarketCoin>.toRows(
    quotes: Map<String, LiveQuote>,
    query: String,
): List<MarketRow> {
    val filtered = if (query.isBlank()) {
        this
    } else {
        filter { coin ->
            coin.name.contains(query, ignoreCase = true) ||
                coin.symbol.contains(query, ignoreCase = true)
        }
    }
    return filtered.map { coin ->
        val quote = quotes[coin.symbol.uppercase()]
        MarketRow(
            coin = coin,
            displayPrice = quote?.price ?: coin.currentPrice,
            isLive = quote != null,
        )
    }
}

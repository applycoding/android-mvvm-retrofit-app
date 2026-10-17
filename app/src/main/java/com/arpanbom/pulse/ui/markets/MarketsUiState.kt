package com.arpanbom.pulse.ui.markets

import com.arpanbom.pulse.domain.model.MarketCoin
import com.arpanbom.pulse.domain.model.MarketError

data class MarketRow(
    val coin: MarketCoin,
    val displayPrice: Double,
    val isLive: Boolean,
)

data class MarketsUiState(
    val coins: List<MarketRow> = emptyList(),
    val query: String = "",
    val isRefreshing: Boolean = false,
    val hasLoaded: Boolean = false,
    val isOnline: Boolean = true,
    val error: MarketError? = null,
    val usedSampleData: Boolean = false,
    val isLive: Boolean = false,
)

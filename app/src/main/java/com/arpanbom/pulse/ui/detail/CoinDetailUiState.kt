package com.arpanbom.pulse.ui.detail

import com.arpanbom.pulse.domain.model.MarketCoin

data class CoinDetailUiState(
    val isLoading: Boolean = true,
    val coin: MarketCoin? = null,
    val displayPrice: Double? = null,
    val isLive: Boolean = false,
)

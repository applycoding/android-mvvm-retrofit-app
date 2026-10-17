package com.arpanbom.pulse.ui.markets

import com.arpanbom.pulse.domain.model.MarketCoin

internal fun bitcoin(): MarketCoin = coin(
    id = "bitcoin",
    symbol = "btc",
    name = "Bitcoin",
    price = 64_250.0,
    change = 1.8,
    rank = 1,
)

internal fun ethereum(): MarketCoin = coin(
    id = "ethereum",
    symbol = "eth",
    name = "Ethereum",
    price = 3_120.0,
    change = -0.6,
    rank = 2,
)

private fun coin(
    id: String,
    symbol: String,
    name: String,
    price: Double,
    change: Double,
    rank: Int,
): MarketCoin = MarketCoin(
    id = id,
    symbol = symbol,
    name = name,
    imageUrl = "",
    currentPrice = price,
    priceChangePercent24h = change,
    marketCap = 1_000_000_000.0,
    volume24h = 10_000_000.0,
    high24h = price * 1.02,
    low24h = price * 0.98,
    rank = rank,
    lastUpdatedEpochMs = 0L,
)

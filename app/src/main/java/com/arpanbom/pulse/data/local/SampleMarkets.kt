package com.arpanbom.pulse.data.local

import com.arpanbom.pulse.data.local.entity.MarketEntity

/**
 * Bundled placeholders used only when a refresh fails and Room has nothing saved.
 * Prices are rounded illustrations, not a market feed.
 */
internal object SampleMarkets {
    fun entities(): List<MarketEntity> = listOf(
        sample("bitcoin", "btc", "Bitcoin", 1, 64_250.0, 1.8, 1_270_000_000_000.0),
        sample("ethereum", "eth", "Ethereum", 2, 3_120.0, -0.6, 375_000_000_000.0),
        sample("tether", "usdt", "Tether", 3, 1.0, 0.0, 120_000_000_000.0),
        sample("binancecoin", "bnb", "BNB", 4, 580.0, 0.4, 84_000_000_000.0),
        sample("solana", "sol", "Solana", 5, 148.0, 2.1, 70_000_000_000.0),
        sample("ripple", "xrp", "XRP", 6, 0.62, -1.1, 35_000_000_000.0),
        sample("dogecoin", "doge", "Dogecoin", 7, 0.12, 3.4, 17_000_000_000.0),
        sample("cardano", "ada", "Cardano", 8, 0.45, -0.8, 16_000_000_000.0),
    )

    private fun sample(
        id: String,
        symbol: String,
        name: String,
        rank: Int,
        price: Double,
        change: Double,
        marketCap: Double,
    ): MarketEntity = MarketEntity(
        id = id,
        symbol = symbol,
        name = name,
        imageUrl = "",
        currentPrice = price,
        priceChangePercent24h = change,
        marketCap = marketCap,
        volume24h = marketCap * 0.04,
        high24h = price * 1.02,
        low24h = price * 0.98,
        rank = rank,
        lastUpdatedEpochMs = 0L,
    )
}

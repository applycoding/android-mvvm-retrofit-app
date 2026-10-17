package com.arpanbom.pulse.domain.model

data class MarketCoin(
    val id: String,
    val symbol: String,
    val name: String,
    val imageUrl: String,
    val currentPrice: Double,
    val priceChangePercent24h: Double,
    val marketCap: Double,
    val volume24h: Double,
    val high24h: Double,
    val low24h: Double,
    val rank: Int,
    val lastUpdatedEpochMs: Long,
)

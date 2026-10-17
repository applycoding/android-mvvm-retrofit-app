package com.arpanbom.pulse.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "markets")
data class MarketEntity(
    @PrimaryKey val id: String,
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

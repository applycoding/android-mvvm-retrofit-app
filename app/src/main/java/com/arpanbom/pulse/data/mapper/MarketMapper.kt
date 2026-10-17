package com.arpanbom.pulse.data.mapper

import com.arpanbom.pulse.data.local.entity.MarketEntity
import com.arpanbom.pulse.data.remote.dto.MarketDto
import com.arpanbom.pulse.domain.model.MarketCoin
import java.time.Instant

fun MarketDto.toEntity(nowEpochMs: Long = System.currentTimeMillis()): MarketEntity = MarketEntity(
    id = id,
    symbol = symbol.lowercase(),
    name = name,
    imageUrl = image.orEmpty(),
    currentPrice = currentPrice ?: 0.0,
    priceChangePercent24h = priceChangePercentage24h ?: 0.0,
    marketCap = marketCap ?: 0.0,
    volume24h = totalVolume ?: 0.0,
    high24h = high24h ?: 0.0,
    low24h = low24h ?: 0.0,
    rank = marketCapRank ?: 0,
    lastUpdatedEpochMs = parseIsoEpochMillis(lastUpdated) ?: nowEpochMs,
)

fun MarketEntity.toDomain(): MarketCoin = MarketCoin(
    id = id,
    symbol = symbol,
    name = name,
    imageUrl = imageUrl,
    currentPrice = currentPrice,
    priceChangePercent24h = priceChangePercent24h,
    marketCap = marketCap,
    volume24h = volume24h,
    high24h = high24h,
    low24h = low24h,
    rank = rank,
    lastUpdatedEpochMs = lastUpdatedEpochMs,
)

fun parseIsoEpochMillis(value: String?): Long? {
    if (value.isNullOrBlank()) return null
    return runCatching { Instant.parse(value).toEpochMilli() }.getOrNull()
}

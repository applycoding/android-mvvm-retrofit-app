package com.arpanbom.pulse.domain.repository

import com.arpanbom.pulse.domain.model.LiveQuote
import com.arpanbom.pulse.domain.model.MarketCoin
import com.arpanbom.pulse.domain.model.RefreshResult
import kotlinx.coroutines.flow.Flow

interface MarketRepository {
    fun observeMarkets(): Flow<List<MarketCoin>>

    fun observeCoin(id: String): Flow<MarketCoin?>

    suspend fun refresh(): RefreshResult

    fun observeLiveQuotes(symbols: Set<String>): Flow<LiveQuote>
}

package com.arpanbom.pulse.domain.usecase

import com.arpanbom.pulse.domain.model.LiveQuote
import com.arpanbom.pulse.domain.model.MarketCoin
import com.arpanbom.pulse.domain.model.RefreshResult
import com.arpanbom.pulse.domain.repository.MarketRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.scan

class ObserveMarketsUseCase @Inject constructor(
    private val repository: MarketRepository,
) {
    operator fun invoke(): Flow<List<MarketCoin>> = repository.observeMarkets()
}

class ObserveCoinUseCase @Inject constructor(
    private val repository: MarketRepository,
) {
    operator fun invoke(id: String): Flow<MarketCoin?> = repository.observeCoin(id)
}

class RefreshMarketsUseCase @Inject constructor(
    private val repository: MarketRepository,
) {
    suspend operator fun invoke(): RefreshResult = repository.refresh()
}

class ObserveLiveQuotesUseCase @Inject constructor(
    private val repository: MarketRepository,
) {
    operator fun invoke(symbols: Set<String>): Flow<Map<String, LiveQuote>> {
        if (symbols.isEmpty()) return flowOf(emptyMap())
        return repository.observeLiveQuotes(symbols)
            .scan(emptyMap<String, LiveQuote>()) { quotes, quote ->
                quotes + (quote.symbol to quote)
            }
            .onStart { emit(emptyMap()) }
    }
}

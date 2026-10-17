package com.arpanbom.pulse.domain.repository

import com.arpanbom.pulse.domain.model.LiveQuote
import kotlinx.coroutines.flow.Flow

/**
 * A live last-price stream. Implementations own reconnect behavior.
 * Symbols are uppercase asset tickers such as "BTC", without a quote currency.
 */
interface LivePriceStream {
    fun observe(symbols: Set<String>): Flow<LiveQuote>
}

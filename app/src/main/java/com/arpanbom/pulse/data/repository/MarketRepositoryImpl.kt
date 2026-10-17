package com.arpanbom.pulse.data.repository

import com.arpanbom.pulse.data.local.SampleMarkets
import com.arpanbom.pulse.data.local.dao.MarketDao
import com.arpanbom.pulse.data.mapper.toDomain
import com.arpanbom.pulse.data.mapper.toEntity
import com.arpanbom.pulse.data.remote.CoinGeckoApi
import com.arpanbom.pulse.data.remote.websocket.SupportedBinanceAssets
import com.arpanbom.pulse.di.IoDispatcher
import com.arpanbom.pulse.domain.model.LiveQuote
import com.arpanbom.pulse.domain.model.MarketCoin
import com.arpanbom.pulse.domain.model.MarketError
import com.arpanbom.pulse.domain.model.RefreshResult
import com.arpanbom.pulse.domain.repository.LivePriceStream
import com.arpanbom.pulse.domain.repository.MarketRepository
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import retrofit2.HttpException

@Singleton
class MarketRepositoryImpl @Inject constructor(
    private val api: CoinGeckoApi,
    private val dao: MarketDao,
    private val livePriceStream: LivePriceStream,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : MarketRepository {
    override fun observeMarkets(): Flow<List<MarketCoin>> =
        dao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override fun observeCoin(id: String): Flow<MarketCoin?> =
        dao.observeById(id).map { row -> row?.toDomain() }

    override suspend fun refresh(): RefreshResult = withContext(ioDispatcher) {
        try {
            val entities = api.markets().map { it.toEntity() }
            if (entities.isEmpty()) {
                error("CoinGecko returned no markets")
            }
            dao.replaceAll(entities)
            RefreshResult.Success
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (throwable: Exception) {
            RefreshResult.Failed(
                error = throwable.toMarketError(),
                usedSampleData = seedSampleIfEmpty(),
            )
        }
    }

    override fun observeLiveQuotes(symbols: Set<String>): Flow<LiveQuote> {
        val supported = symbols
            .map { it.uppercase().removeSuffix("USDT") }
            .filter { it in SupportedBinanceAssets }
            .toSet()
        if (supported.isEmpty()) return emptyFlow()
        return livePriceStream.observe(supported)
    }

    private suspend fun seedSampleIfEmpty(): Boolean {
        if (dao.count() > 0) return false
        dao.upsertAll(SampleMarkets.entities())
        return true
    }
}

private fun Throwable.toMarketError(): MarketError = when (this) {
    is HttpException -> if (code() == 429) MarketError.RateLimited else MarketError.Server
    is IOException -> MarketError.Offline
    else -> MarketError.Unknown
}

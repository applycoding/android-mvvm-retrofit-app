package com.arpanbom.pulse.di

import com.arpanbom.pulse.data.connectivity.ConnectivityNetworkStatus
import com.arpanbom.pulse.data.remote.websocket.BinanceLivePriceSocket
import com.arpanbom.pulse.data.repository.MarketRepositoryImpl
import com.arpanbom.pulse.domain.repository.LivePriceStream
import com.arpanbom.pulse.domain.repository.MarketRepository
import com.arpanbom.pulse.domain.repository.NetworkStatus
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindMarketRepository(impl: MarketRepositoryImpl): MarketRepository

    @Binds
    @Singleton
    abstract fun bindLivePriceStream(impl: BinanceLivePriceSocket): LivePriceStream

    @Binds
    @Singleton
    abstract fun bindNetworkStatus(impl: ConnectivityNetworkStatus): NetworkStatus
}

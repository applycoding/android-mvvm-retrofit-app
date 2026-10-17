package com.arpanbom.pulse.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class HttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class WsClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CoinGeckoBaseUrl

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BinanceWsBaseUrl

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CoinGeckoApiKey

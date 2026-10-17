package com.arpanbom.pulse.di

import android.content.Context
import com.arpanbom.pulse.BuildConfig
import com.arpanbom.pulse.data.remote.CoinGeckoApi
import com.arpanbom.pulse.data.remote.interceptor.AuthInterceptor
import com.arpanbom.pulse.data.remote.interceptor.CacheControlInterceptor
import com.arpanbom.pulse.data.remote.websocket.RetryPolicy
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    @CoinGeckoApiKey
    fun provideApiKey(): String = BuildConfig.COINGECKO_API_KEY

    @Provides
    @Singleton
    @CoinGeckoBaseUrl
    fun provideCoinGeckoBaseUrl(): String = BuildConfig.COINGECKO_BASE_URL

    @Provides
    @Singleton
    @BinanceWsBaseUrl
    fun provideBinanceWsBaseUrl(): String = BuildConfig.BINANCE_WS_URL

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        isLenient = true
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(@CoinGeckoApiKey apiKey: String): AuthInterceptor =
        AuthInterceptor(apiKey)

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            redactHeader("x-cg-demo-api-key")
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.HEADERS
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

    @Provides
    @Singleton
    @HttpClient
    fun provideHttpClient(
        @ApplicationContext context: Context,
        authInterceptor: AuthInterceptor,
        cacheControlInterceptor: CacheControlInterceptor,
        loggingInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient {
        val cache = Cache(File(context.cacheDir, "http-cache"), HTTP_CACHE_BYTES)
        return OkHttpClient.Builder()
            .cache(cache)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .addNetworkInterceptor(cacheControlInterceptor)
            .build()
    }

    @Provides
    @Singleton
    @WsClient
    fun provideWsClient(): OkHttpClient =
        OkHttpClient.Builder()
            .pingInterval(20, TimeUnit.SECONDS)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .retryOnConnectionFailure(true)
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(
        @HttpClient client: OkHttpClient,
        @CoinGeckoBaseUrl baseUrl: String,
        json: Json,
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    fun provideCoinGeckoApi(retrofit: Retrofit): CoinGeckoApi =
        retrofit.create(CoinGeckoApi::class.java)

    @Provides
    @Singleton
    fun provideRetryPolicy(): RetryPolicy = RetryPolicy()

    private const val HTTP_CACHE_BYTES = 10L * 1024L * 1024L
}

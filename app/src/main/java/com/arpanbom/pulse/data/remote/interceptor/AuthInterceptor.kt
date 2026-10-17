package com.arpanbom.pulse.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Adds a CoinGecko demo key when one is configured. The public demo ships with an empty key
 * and calls the unauthenticated API.
 */
class AuthInterceptor(
    private val apiKey: String,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = if (apiKey.isBlank()) {
            chain.request()
        } else {
            chain.request().newBuilder()
                .header(HEADER, apiKey)
                .build()
        }
        return chain.proceed(request)
    }

    private companion object {
        const val HEADER = "x-cg-demo-api-key"
    }
}

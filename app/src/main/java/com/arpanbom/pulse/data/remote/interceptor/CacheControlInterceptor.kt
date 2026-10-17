package com.arpanbom.pulse.data.remote.interceptor

import javax.inject.Inject
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Gives successful responses a short cache lifetime when the server omits Cache-Control,
 * so OkHttp can reuse the last payload across brief reconnects. Room remains the offline store.
 */
class CacheControlInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (!response.header("Cache-Control").isNullOrBlank()) return response
        return response.newBuilder()
            .header("Cache-Control", "public, max-age=60")
            .build()
    }
}

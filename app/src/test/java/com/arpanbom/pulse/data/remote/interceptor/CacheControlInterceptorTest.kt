package com.arpanbom.pulse.data.remote.interceptor

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Test

class CacheControlInterceptorTest {
    @Test
    fun setsShortCacheWhenServerOmitsIt() {
        val server = MockWebServer()
        val client = OkHttpClient.Builder()
            .addNetworkInterceptor(CacheControlInterceptor())
            .build()
        server.start()
        try {
            server.enqueue(MockResponse().setBody("[]"))
            val response = client.newCall(Request.Builder().url(server.url("/")).build()).execute()
            response.use {
                assertEquals("public, max-age=60", it.header("Cache-Control"))
            }
        } finally {
            client.dispatcher.executorService.shutdown()
            client.connectionPool.evictAll()
            server.shutdown()
        }
    }

    @Test
    fun keepsServerCacheHeader() {
        val server = MockWebServer()
        val client = OkHttpClient.Builder()
            .addNetworkInterceptor(CacheControlInterceptor())
            .build()
        server.start()
        try {
            server.enqueue(MockResponse().setBody("[]").setHeader("Cache-Control", "max-age=5"))
            val response = client.newCall(Request.Builder().url(server.url("/")).build()).execute()
            response.use {
                assertEquals("max-age=5", it.header("Cache-Control"))
            }
        } finally {
            client.dispatcher.executorService.shutdown()
            client.connectionPool.evictAll()
            server.shutdown()
        }
    }
}

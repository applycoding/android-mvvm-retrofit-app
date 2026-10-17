package com.arpanbom.pulse.data.remote.interceptor

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthInterceptorTest {
    @Test
    fun addsDemoKeyWhenConfigured() {
        val server = MockWebServer()
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor("demo-key"))
            .build()
        server.start()
        try {
            server.enqueue(MockResponse().setBody("[]"))
            client.newCall(Request.Builder().url(server.url("/coins/markets")).build()).execute().close()
            assertEquals("demo-key", server.takeRequest().getHeader("x-cg-demo-api-key"))
        } finally {
            client.dispatcher.executorService.shutdown()
            client.connectionPool.evictAll()
            server.shutdown()
        }
    }

    @Test
    fun omitsHeaderWhenKeyIsBlank() {
        val server = MockWebServer()
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor("  "))
            .build()
        server.start()
        try {
            server.enqueue(MockResponse().setBody("[]"))
            client.newCall(Request.Builder().url(server.url("/coins/markets")).build()).execute().close()
            assertNull(server.takeRequest().getHeader("x-cg-demo-api-key"))
        } finally {
            client.dispatcher.executorService.shutdown()
            client.connectionPool.evictAll()
            server.shutdown()
        }
    }
}

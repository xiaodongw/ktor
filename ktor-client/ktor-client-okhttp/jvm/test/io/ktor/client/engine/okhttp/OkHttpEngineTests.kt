/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.client.engine.okhttp

import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.client.request.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.*
import okhttp3.*
import java.util.concurrent.*
import kotlin.test.*

class OkHttpEngineTests {
    @Test
    fun testClose() {
        val okHttpClient = OkHttpClient()
        val engine = OkHttpEngine(OkHttpConfig().apply { preconfigured = okHttpClient })
        engine.close()

        assertFalse("OkHttp dispatcher is not working.") { okHttpClient.dispatcher.executorService.isShutdown }
        assertEquals(0, okHttpClient.connectionPool.connectionCount())
        okHttpClient.cache?.let { assertFalse("OkHttp client cache is closed.") { it.isClosed } }
    }

    @Test
    fun testThreadLeak() = runBlocking {
        val initialNumberOfThreads = Thread.getAllStackTraces().size

        repeat(25) {
            HttpClient(OkHttp).use { client ->
                val response = client.get<String>("http://www.google.com")
                assertNotNull(response)
            }
        }

        val totalNumberOfThreads = Thread.getAllStackTraces().size
        val threadsCreated = totalNumberOfThreads - initialNumberOfThreads
        assertTrue { threadsCreated < 25 }
    }

    @Test
    fun testPreconfigured() = runBlocking {
        var preconfiguredClientCalled = false
        val okHttpClient = OkHttpClient().newBuilder().addInterceptor(object : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                preconfiguredClientCalled = true
                return chain.proceed(chain.request())
            }
        }).connectTimeout(1, TimeUnit.MILLISECONDS).build()

        HttpClient(OkHttp) {
            engine { preconfigured = okHttpClient }
        }.use { client ->
            runCatching { client.get<String>("http://localhost:1234") }
            assertTrue(preconfiguredClientCalled)
        }
    }

    @Test
    fun testRequestAfterRecreate() {
        runBlocking {
            HttpClient(OkHttp)
                .close()

            HttpClient(OkHttp).use { client ->
                val response = client.get<String>("http://www.google.com")
                assertNotNull(response)
            }
        }
    }
}

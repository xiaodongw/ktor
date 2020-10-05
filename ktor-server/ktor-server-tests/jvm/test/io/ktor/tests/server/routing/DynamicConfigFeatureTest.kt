/*
 * Copyright 2014-2020 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.tests.server.routing

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.testing.*
import io.ktor.util.*
import kotlin.test.*

class DynamicConfigFeatureTest {

    @Test
    fun testFeatureInstalledTopLevel() = withTestApplication {
        val callbackResults = mutableListOf<String>()
        val receiveCallbackResults = mutableListOf<String>()
        val sendCallbackResults = mutableListOf<String>()
        val allCallbacks = listOf(callbackResults, receiveCallbackResults, sendCallbackResults)

        application.install(TestFeature) {
            name = "foo"
            desc = "test feature"
            pipelineCallback = { callbackResults.add(it) }
            receivePipelineCallback = { receiveCallbackResults.add(it) }
            sendPipelineCallback = { sendCallbackResults.add(it) }
        }

        application.routing {
            route("root") {
                handle {
                    call.respond(call.receive<String>())
                }

                route("feature1") {
                    config(TestFeature) { name = "bar" }

                    handle {
                        call.respond(call.receive<String>())
                    }
                }

                route("feature2") {
                    config(TestFeature) { name = "baz" }

                    handle {
                        call.respond(call.receive<String>())
                    }
                }
            }

            handle {
                call.respond(call.receive<String>())
            }
        }

        on("making get request to /root") {
            val result = handleRequest {
                uri = "/root"
                method = HttpMethod.Post
                setBody("test")
            }
            it("should be handled") {
                assertTrue(result.requestHandled)
            }
            it("callback should be invoked") {
                allCallbacks.forEach {
                    assertEquals(1, it.size)
                    assertEquals("foo test feature", it[0])
                    it.clear()
                }
            }
        }

        on("making get request to /root/feature1") {
            val result = handleRequest {
                uri = "/root/feature1"
                method = HttpMethod.Post
                setBody("test")
            }
            it("should be handled") {
                assertTrue(result.requestHandled)
            }
            it("callback should be invoked") {
                allCallbacks.forEach {
                    assertEquals(1, it.size)
                    assertEquals("bar test feature", it[0])
                    it.clear()
                }
            }
        }

        on("making get request to /root/feature2") {
            val result = handleRequest {
                uri = "/root/feature2"
                method = HttpMethod.Post
                setBody("test")
            }
            it("should be handled") {
                assertTrue(result.requestHandled)
            }
            it("callback should be invoked") {
                allCallbacks.forEach {
                    assertEquals(1, it.size)
                    assertEquals("baz test feature", it[0])
                    it.clear()
                }
            }
        }
    }

    @Test
    fun testFeatureInstalledInRoutingScope() = withTestApplication {
        val callbackResults = mutableListOf<String>()
        val receiveCallbackResults = mutableListOf<String>()
        val sendCallbackResults = mutableListOf<String>()
        val allCallbacks = listOf(callbackResults, receiveCallbackResults, sendCallbackResults)

        application.routing {
            route("root-no-feature") {
                route("first-feature") {
                    install(TestFeature) {
                        name = "foo"
                        desc = "test feature"
                        pipelineCallback = { callbackResults.add(it) }
                        receivePipelineCallback = { receiveCallbackResults.add(it) }
                        sendPipelineCallback = { sendCallbackResults.add(it) }
                    }

                    handle {
                        call.respond(call.receive<String>())
                    }

                    route("inner") {
                        route("new-feature") {
                            config(TestFeature, { name = "bar" })

                            route("inner") {
                                handle {
                                    call.respond(call.receive<String>())
                                }
                            }

                            handle {
                                call.respond(call.receive<String>())
                            }
                        }

                        handle {
                            call.respond(call.receive<String>())
                        }
                    }
                }

                handle {
                    call.respond(call.receive<String>())
                }
            }
        }

        on("making get request to /root-no-feature") {
            val result = handleRequest {
                uri = "/root-no-feature"
                method = HttpMethod.Post
                setBody("test")
            }
            it("should be handled") {
                assertTrue(result.requestHandled)
            }
            it("callback should not be invoked") {
                allCallbacks.forEach {
                    assertEquals(0, it.size)
                }
            }
        }

        on("making get request to /root-no-feature/first-feature") {
            val result = handleRequest {
                uri = "/root-no-feature/first-feature"
                method = HttpMethod.Post
                setBody("test")
            }
            it("should be handled") {
                assertTrue(result.requestHandled)
            }
            it("callback should be invoked") {
                allCallbacks.forEach {
                    assertEquals(1, it.size)
                    assertEquals("foo test feature", it[0])
                    it.clear()
                }
            }
        }

        on("making get request to /root-no-feature/first-feature/inner") {
            val result = handleRequest {
                uri = "/root-no-feature/first-feature/inner"
                method = HttpMethod.Post
                setBody("test")
            }
            it("should be handled") {
                assertTrue(result.requestHandled)
            }
            it("callback should be invoked") {
                allCallbacks.forEach {
                    assertEquals(1, it.size)
                    assertEquals("foo test feature", it[0])
                    it.clear()
                }
            }
        }

        on("making get request to /root-no-feature/first-feature/inner/new-feature") {
            val result = handleRequest {
                uri = "/root-no-feature/first-feature/inner/new-feature"
                method = HttpMethod.Post
                setBody("test")
            }
            it("should be handled") {
                assertTrue(result.requestHandled)
            }
            it("callback should be invoked") {
                allCallbacks.forEach {
                    assertEquals(1, it.size)
                    assertEquals("bar test feature", it[0])
                    it.clear()
                }
            }
        }

        on("making get request to /root-no-feature/first-feature/inner/new-feature/inner") {
            val result = handleRequest {
                uri = "/root-no-feature/first-feature/inner/new-feature/inner"
                method = HttpMethod.Post
                setBody("test")
            }
            it("should be handled") {
                assertTrue(result.requestHandled)
            }
            it("callback should be invoked") {
                allCallbacks.forEach {
                    assertEquals(1, it.size)
                    assertEquals("bar test feature", it[0])
                    it.clear()
                }
            }
        }
    }

    @Test
    fun testFeatureReuseConfig() = withTestApplication {
        val callbackResults = mutableListOf<String>()
        val receiveCallbackResults = mutableListOf<String>()
        val sendCallbackResults = mutableListOf<String>()
        val allCallbacks = listOf(callbackResults, receiveCallbackResults, sendCallbackResults)

        application.routing {
            route("root") {
                install(TestFeature) {
                    name = "foo"
                    desc = "test feature"
                    pipelineCallback = { callbackResults.add(it) }
                    receivePipelineCallback = { receiveCallbackResults.add(it) }
                    sendPipelineCallback = { sendCallbackResults.add(it) }
                }
                route("feature1") {
                    config(TestFeature, { desc = "new desc" })

                    handle {
                        call.respond(call.receive<String>())
                    }

                    route("feature2") {
                        config(TestFeature, { name = "bar" })

                        handle {
                            call.respond(call.receive<String>())
                        }
                    }
                }

                handle {
                    call.respond(call.receive<String>())
                }
            }
        }

        on("making get request to /root") {
            val result = handleRequest {
                uri = "/root"
                method = HttpMethod.Post
                setBody("test")
            }
            it("should be handled") {
                assertTrue(result.requestHandled)
            }
            it("callback should be invoked") {
                allCallbacks.forEach {
                    assertEquals(1, it.size)
                    assertEquals("foo test feature", it[0])
                    it.clear()
                }
            }
        }

        on("making get request to /root/feature1") {
            val result = handleRequest {
                uri = "/root/feature1"
                method = HttpMethod.Post
                setBody("test")
            }
            it("should be handled") {
                assertTrue(result.requestHandled)
            }
            it("callback should be invoked") {
                allCallbacks.forEach {
                    assertEquals(1, it.size)
                    assertEquals("foo new desc", it[0])
                    it.clear()
                }
            }
        }

        on("making get request to /root/feature1/feature2") {
            val result = handleRequest {
                uri = "/root/feature1/feature2"
                method = HttpMethod.Post
                setBody("test")
            }
            it("should be handled") {
                assertTrue(result.requestHandled)
            }
            it("callback should be invoked") {
                allCallbacks.forEach {
                    assertEquals(1, it.size)
                    assertEquals("bar new desc", it[0])
                    it.clear()
                }
            }
        }
    }
}

class TestFeature {

    fun install(pipeline: ApplicationCallPipeline) {
        pipeline.intercept(ApplicationCallPipeline.Features) {
            val config = Config().apply(getConfiguration())
            config.pipelineCallback("${config.name} ${config.desc}")
        }
        pipeline.receivePipeline.intercept(ApplicationReceivePipeline.Before) {
            val config = Config().apply(getConfiguration())
            config.receivePipelineCallback("${config.name} ${config.desc}")
        }
        pipeline.sendPipeline.intercept(ApplicationSendPipeline.Before) {
            val config = Config().apply(getConfiguration())
            config.sendPipelineCallback("${config.name} ${config.desc}")
        }
    }

    data class Config(
        var name: String = "",
        var desc: String = "",
        var pipelineCallback: (String) -> Unit = {},
        var receivePipelineCallback: (String) -> Unit = {},
        var sendPipelineCallback: (String) -> Unit = {},
    )

    companion object Feature : DynamicConfigFeature<ApplicationCallPipeline, Config, TestFeature> {

        override val key: AttributeKey<TestFeature>
            get() = AttributeKey("TestFeature")

        override fun install(pipeline: ApplicationCallPipeline): TestFeature {
            val feature = TestFeature()
            return feature.apply { install(pipeline) }
        }
    }
}

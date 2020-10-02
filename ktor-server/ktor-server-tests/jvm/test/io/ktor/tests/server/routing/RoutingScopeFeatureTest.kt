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

class RoutingScopeFeatureTest {

    @Test
    fun testFeatureScope() = withTestApplication {
        val callbackResults = mutableListOf<String>()
        val receiveCallbackResults = mutableListOf<String>()
        val sendCallbackResults = mutableListOf<String>()
        val allCallbacks = setOf(callbackResults, receiveCallbackResults, sendCallbackResults)

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

                    get {
                        call.respond(HttpStatusCode.OK)
                    }

                    route("inner") {
                        route("new-feature") {
                            config(TestFeature, { name = "bar" }) {
                                get("inner") {
                                    call.respond(HttpStatusCode.OK)
                                }

                                handle {
                                    call.respond(HttpStatusCode.OK)
                                }
                            }
                        }

                        handle {
                            call.respond(HttpStatusCode.OK)
                        }
                    }
                }

                handle {
                    call.respond(HttpStatusCode.OK)
                }
            }
        }

        on("making get request to /root-no-feature") {
            val result = handleRequest {
                uri = "/root-no-feature"
                method = HttpMethod.Get
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
                method = HttpMethod.Get
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
                method = HttpMethod.Get
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
                method = HttpMethod.Get
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
                method = HttpMethod.Get
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

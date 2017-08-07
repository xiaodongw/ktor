package org.jetbrains.ktor.servlet

import kotlinx.coroutines.experimental.*
import org.jetbrains.ktor.cio.*
import org.jetbrains.ktor.content.*
import org.jetbrains.ktor.host.*
import org.jetbrains.ktor.http.*
import org.jetbrains.ktor.response.*
import org.jetbrains.ktor.util.*
import java.io.*
import javax.servlet.http.*
import kotlin.coroutines.experimental.*

open class ServletApplicationResponse(call: ServletApplicationCall,
                                      protected val servletRequest: HttpServletRequest,
                                      protected val servletResponse: HttpServletResponse,
                                      protected val hostCoroutineContext: CoroutineContext,
                                      protected val userCoroutineContext: CoroutineContext,
                                      private val pushImpl: (ResponsePushBuilder) -> Boolean
) : BaseApplicationResponse(call) {
    override fun setStatus(statusCode: HttpStatusCode) {
        servletResponse.status = statusCode.value
    }

    override val headers: ResponseHeaders = object : ResponseHeaders() {
        override fun hostAppendHeader(name: String, value: String) {
            servletResponse.addHeader(name, value)
        }

        override fun getHostHeaderNames(): List<String> = servletResponse.headerNames.toList()
        override fun getHostHeaderValues(name: String): List<String> = servletResponse.getHeaders(name).toList()
    }

    @Volatile
    private var completed: Boolean = false

    suspend override fun respondUpgrade(upgrade: FinalContent.ProtocolUpgrade) {
        servletResponse.status = upgrade.status?.value ?: HttpStatusCode.SwitchingProtocols.value
        upgrade.headers.flattenEntries().forEach { e ->
            servletResponse.addHeader(e.first, e.second)
        }

        servletResponse.flushBuffer()
        val handler = servletRequest.upgrade(ServletUpgradeHandler::class.java)
        handler.up = UpgradeRequest(servletResponse, upgrade, hostCoroutineContext, userCoroutineContext)

//        servletResponse.flushBuffer()

        completed = true
//        servletRequest.asyncContext?.complete() // causes pipeline execution break however it is required for websocket
    }

    private val responseChannel = lazy {
        ServletWriteChannel(servletResponse.outputStream)
    }

    override suspend fun responseChannel(): WriteChannel = responseChannel.value

    init {
        pipeline.intercept(ApplicationSendPipeline.Host) {
            if (!completed) {
                completed = true
                //request.close()
                if (responseChannel.isInitialized()) {
                    responseChannel.value.apply {
                        flush()
                        close()
                    }
                } else {
                    servletResponse.flushBuffer()
                }
            }
        }
    }

    // the following types need to be public as they are accessed through reflection

    class UpgradeRequest(val response: HttpServletResponse,
                         val upgradeMessage: FinalContent.ProtocolUpgrade,
                         val hostContext: CoroutineContext,
                         val userAppContext: CoroutineContext)

    class ServletUpgradeHandler : HttpUpgradeHandler {
        @Volatile
        lateinit var up: UpgradeRequest

        override fun init(webConnection: WebConnection?) {
            if (webConnection == null) {
                throw IllegalArgumentException("Upgrade processing requires WebConnection instance")
            }

            val inputChannel = ServletReadChannel(webConnection.inputStream)
            val outputChannel = ServletWriteChannel(webConnection.outputStream)

            runBlocking {
                up.upgradeMessage.upgrade(inputChannel, outputChannel, Closeable { webConnection.close() }, up.hostContext, up.userAppContext)
            }
        }

        override fun destroy() {
        }
    }

    override fun push(builder: ResponsePushBuilder) {
        if (!pushImpl(builder)) {
            super.push(builder)
        }
    }
}

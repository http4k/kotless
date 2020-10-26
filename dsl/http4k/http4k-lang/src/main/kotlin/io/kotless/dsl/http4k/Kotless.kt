package io.kotless.dsl.http4k

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestStreamHandler
import io.kotless.InternalAPI
import io.kotless.dsl.model.CloudWatch
import io.kotless.dsl.model.HttpRequest
import io.kotless.dsl.model.HttpResponse
import io.kotless.dsl.utils.Json
import org.http4k.core.HttpHandler
import org.http4k.core.RequestContexts
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.io.OutputStream

/**
 * Entrypoint of Kotless application written with http4k DSL.
 *
 * Implement [handler] method and setup your application
 */
@Suppress("unused")
abstract class Kotless(private val env: Map<String, String> = System.getenv()) : RequestStreamHandler {

    /**
     * Build the HttpHandler for this function, using the Environment and the RequestContexts
     * (contains the original context passed to the function)
     */
    abstract fun handler(env: Map<String, String>, requestContexts: RequestContexts): HttpHandler

    companion object {
        const val KOTLESS_CONTEXT_KEY = "HTTP4K_KOTLESS_CONTEXT"
        const val KOTLESS_REQUEST_KEY = "HTTP4K_KOTLESS_REQUEST"

        private val logger = LoggerFactory.getLogger(Kotless::class.java)
        private var function: KotlessHttp4kFunction? = null
    }

    @InternalAPI
    override fun handleRequest(input: InputStream, output: OutputStream, context: Context) {
        val json = input.bufferedReader().use { it.readText() }

        logger.debug("Started handling request")
        logger.trace("Request is {}", json)

        if (json.contains("Scheduled Event")) {
            val event = Json.parse(CloudWatch.serializer(), json)
            if (event.`detail-type` == "Scheduled Event" && event.source == "aws.events") {
                logger.debug("Request is Scheduled Event. Nothing to do during warming")
                return
            }
        }

        logger.debug("Request is HTTP Event")

        val request = Json.parse(HttpRequest.serializer(), json)

        val kotlessHttp4kFunction = function
            ?: (KotlessHttp4kFunction(handler(System.getenv(), RequestContexts()))
                .also { function = it })

        output.write(Json.bytes(HttpResponse.serializer(), kotlessHttp4kFunction.handleRequest(request, context)))
    }

}

package io.kotless.dsl.http4k

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestStreamHandler
import io.kotless.InternalAPI
import io.kotless.MimeType
import io.kotless.dsl.model.CloudWatch
import io.kotless.dsl.model.HttpRequest
import io.kotless.dsl.model.HttpResponse
import io.kotless.dsl.utils.Json
import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.lens.Header.CONTENT_TYPE
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer

/**
 * Entrypoint of Kotless application written with http4k DSL.
 *
 * Implement [handler] method and setup your application
 */
@Suppress("unused")
abstract class Kotless : RequestStreamHandler {
    abstract fun handler(): HttpHandler

    companion object {
        private val logger = LoggerFactory.getLogger(Kotless::class.java)
        private var handler: HttpHandler? = null
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

        val httpHandler = handler ?: handler().also { handler = it }

        val response = httpHandler(Json.parse(HttpRequest.serializer(), json).asHttp4k())

        output.write(Json.bytes(HttpResponse.serializer(), response.asKotless()))
    }
}

private fun HttpRequest.asHttp4k(): Request {
    val base = Request(Method.valueOf(method.name), path)
        .body(body?.let { Body(ByteBuffer.wrap(it)) } ?: Body.EMPTY)

    val withUriAndQueries = (myQueryStringParameters ?: emptyMap()).entries.fold(base) { acc, next ->
        acc.query(next.key, next.value)
    }

    return (headers ?: emptyMap()).entries.fold(withUriAndQueries) { acc, next ->
        next.value.fold(acc) { acc2, nextHeader -> acc2.header(next.key, nextHeader) }
    }
}

private fun Response.asKotless(): HttpResponse {
    val isBinary = CONTENT_TYPE(this)?.let {
        MimeType.forDeclaration(it.value.split("/")[0], it.value.split("/")[1])
    }?.isBinary ?: false

    val hashHeaders = HashMap<String, String>().apply {
        putAll(headers.map { it.first to (it.second ?: "") }.toMap())
    }

    return when {
        isBinary -> HttpResponse(status.code, hashHeaders, body.payload.array())
        else -> HttpResponse(status.code, hashHeaders, bodyString())
    }
}

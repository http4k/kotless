package io.kotless.dsl.http4k

import com.amazonaws.services.lambda.runtime.Context
import io.kotless.MimeType
import io.kotless.dsl.http4k.Kotless.Companion.KOTLESS_CONTEXT_KEY
import io.kotless.dsl.http4k.Kotless.Companion.KOTLESS_REQUEST_KEY
import io.kotless.dsl.model.HttpRequest
import io.kotless.dsl.model.HttpResponse
import org.http4k.core.Body
import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.RequestContexts
import org.http4k.core.Response
import org.http4k.core.then
import org.http4k.filter.ServerFilters.InitialiseRequestContext
import org.http4k.lens.Header
import org.http4k.serverless.AppLoader
import org.http4k.serverless.AppLoaderWithContexts
import java.nio.ByteBuffer

class KotlessHttp4kFunction(appLoader: AppLoaderWithContexts) {
    constructor(input: AppLoader) : this(object : AppLoaderWithContexts {
        override fun invoke(env: Map<String, String>, p2: RequestContexts) = input(env)
    })

    constructor(input: HttpHandler) : this(object : AppLoader {
        override fun invoke(p1: Map<String, String>) = input
    })

    private val contexts = RequestContexts()
    private val app = appLoader(System.getenv(), contexts)

    fun handleRequest(request: HttpRequest, context: Context) =
        InitialiseRequestContext(contexts)
            .then(AddLambdaContextAndRequest(request, context, contexts))
            .then(app)(request.asHttp4k())
            .asKotless()
}

private fun AddLambdaContextAndRequest(request: HttpRequest, ctx: Context?, contexts: RequestContexts) = Filter { next ->
    {
        ctx?.apply { contexts[it][KOTLESS_CONTEXT_KEY] = ctx }
        contexts[it][KOTLESS_REQUEST_KEY] = request
        next(it)
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
    val isBinary = Header.CONTENT_TYPE(this)?.let {
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

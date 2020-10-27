package io.kotless.examples

import io.kotless.dsl.http4k.Kotless
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.RequestContexts
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.routing.bind
import org.http4k.routing.routes

class Server : Kotless() {
//    override fun prepare(app: Application) {
//        app.routing {
//            siteStatics()
//
//            get("/") {
//                call.respondHtml { main() }
//            }
//
//            get("/r") {
//                val k = call.parameters["k"]!!
//
//                val url = URLStorage.getByCode(k)
//                if (url == null) {
//                    call.respond(HttpStatusCode.NotFound, "Unknown short URL")
//                } else {
//                    call.respondRedirect(url)
//                }
//            }
//
//            get("/shorten") {
//                val value = call.parameters["value"]!!
//
//                logger.info("URL for shortening $value")
//
//                val url = if (value.contains("://").not()) "https://$value" else value
//
//                if (UrlValidator.getInstance().isValid(url).not()) {
//                    call.respondText { "Non valid URL" }
//                } else {
//                    val code = URLStorage.getByUrl(url) ?: URLStorage.createCode(url)
//                    call.respondText { "https://ktor.short.kotless.io/r?k=$code" }
//                }
//            }
//        }
//    }

    override fun handler(env: Map<String, String>, requestContexts: RequestContexts): HttpHandler {
        return routes(
            "/r" bind Method.GET to { req: Request -> Response(Status.OK).body("ok") },
            "/shorten" bind Method.GET to { req: Request -> Response(Status.OK).body("ok") },
            "/hello" bind Method.GET to { req: Request -> Response(Status.OK).body("hi there.") },
            "/" bind Method.GET to { req: Request -> Response(Status.OK).body("ok") }
        )
    }
}

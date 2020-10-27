import io.kotless.dsl.http4k.Kotless
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.RequestContexts
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.routing.bind
import org.http4k.routing.routes

class MyFunction : Kotless() {
    override fun handler(env: Map<String, String>, requestContexts: RequestContexts): HttpHandler {
        return routes("/" bind Method.GET to { req: Request -> Response(Status.OK) })
    }
}

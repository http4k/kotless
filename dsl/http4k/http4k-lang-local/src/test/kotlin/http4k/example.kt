package http4k

import io.kotless.Constants
import io.kotless.dsl.http4k.Kotless
import org.http4k.client.Java8HttpClient
import org.http4k.core.HttpHandler
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.RequestContexts
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import java.util.Collections

class Http4kApp : Kotless() {
    override fun handler(env: Map<String, String>, requestContexts: RequestContexts): HttpHandler =
        { req: Request -> Response(OK).body(req.toString()) }
}

fun main() {
    hackEnvironment(mapOf(
        Constants.Local.serverPort to "9000",
        Constants.Local.KtorOrSpringOrHttp4k.classToStart to Http4kApp::class.qualifiedName!!
    ))

    io.kotless.local.http4k.main()

    println(Java8HttpClient()(Request(POST, "http://localhost:9000/echoRequest")))
}

@Suppress("UNCHECKED_CAST")
private fun hackEnvironment(newenv: Map<String, String>) {
    val classes = Collections::class.java.declaredClasses
    val env = System.getenv()
    for (cl in classes) {
        if ("java.util.Collections\$UnmodifiableMap" == cl.name) {
            val field = cl.getDeclaredField("m")
            field.isAccessible = true
            val obj = field[env]
            val map = obj as MutableMap<String, String>
            map.clear()
            map.putAll(newenv)
        }
    }
}

package io.kotless.local.http4k

import io.kotless.Constants
import io.kotless.Constants.Local.KtorOrSpringOrHttp4k
import io.kotless.dsl.http4k.Kotless
import org.http4k.core.RequestContexts
import org.http4k.server.SunHttp
import org.http4k.server.asServer
import kotlin.reflect.full.primaryConstructor

fun main() {
    val classToStart = System.getenv(KtorOrSpringOrHttp4k.classToStart)

    val ktClass = ::main::class.java.classLoader.loadClass(classToStart).kotlin
    val instance = (ktClass.primaryConstructor?.call() ?: ktClass.objectInstance) as? Kotless

    val kotless = instance ?: error("The entry point $classToStart does not inherit from ${Kotless::class.qualifiedName}!")

    val port = System.getenv(Constants.Local.serverPort).toInt()

    kotless.handler(System.getenv(), RequestContexts()).asServer(SunHttp(port)).start()
}

package io.kotless.local.http4k

import io.kotless.Constants
import io.kotless.dsl.http4k.Kotless
import org.http4k.server.SunHttp
import org.http4k.server.asServer
import kotlin.reflect.full.primaryConstructor

fun main() {
    val port = System.getenv(Constants.Local.serverPort).toInt()
    val classToStart = System.getenv(Constants.Local.KtorOrSpringOrHttp4k.classToStart)

    val ktClass = ::main::class.java.classLoader.loadClass(classToStart).kotlin
    val instance = (ktClass.primaryConstructor?.call() ?: ktClass.objectInstance) as? Kotless

    val kotless = instance ?: error("The entry point $classToStart does not inherit from ${Kotless::class.qualifiedName}!")

    kotless.handler().asServer(SunHttp(port)).start()
}

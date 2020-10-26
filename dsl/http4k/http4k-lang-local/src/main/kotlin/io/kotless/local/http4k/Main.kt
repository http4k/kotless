package io.kotless.local.http4k

import io.kotless.Constants
import io.kotless.dsl.http4k.Kotless
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlin.reflect.full.primaryConstructor

fun main() {
    val port = System.getenv(Constants.Local.serverPort).toInt()
    val classToStart = System.getenv(Constants.Local.KtorOrSpringOrHttp4k.classToStart)

    val ktClass = ::main::class.java.classLoader.loadClass(classToStart).kotlin
    val instance = (ktClass.primaryConstructor?.call() ?: ktClass.objectInstance) as? Kotless

    val kotless = instance ?: error("The entry point $classToStart does not inherit from ${Kotless::class.qualifiedName}!")

    kotless.han
    embeddedServer(Netty, port) {
        kotless.prepare(this)
    }.start(wait = true)
}

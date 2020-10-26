package io.kotless.parser.http4k

import io.kotless.parser.DSLDescriptor

object KtorDescriptor : DSLDescriptor {
    override val name: String = "ktor"

    override val parser = KTorParser

    override val localEntryPoint: String = "io.kotless.local.ktor.MainKt"
}

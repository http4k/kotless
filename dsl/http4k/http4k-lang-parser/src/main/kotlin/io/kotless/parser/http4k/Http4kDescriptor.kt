package io.kotless.parser.http4k

import io.kotless.parser.DSLDescriptor

object Http4kDescriptor : DSLDescriptor {
    override val name = "http4k"

    override val parser = Http4kParser

    override val localEntryPoint = "io.kotless.local.http4k.MainKt"
}

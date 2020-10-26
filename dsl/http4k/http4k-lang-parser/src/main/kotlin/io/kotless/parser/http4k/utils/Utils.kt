package io.kotless.parser.http4k.utils

import io.kotless.MimeType
import io.ktor.http.ContentType

fun ContentType.toMime() = MimeType.values().find { it.mimeText == "${contentType}/${contentSubtype}" }

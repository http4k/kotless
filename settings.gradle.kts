rootProject.name = "kotless"

include(":schema")
include(":model")
include(":engine")

include(":dsl:common:lang-common")
include(":dsl:common:lang-parser-common")

include(":dsl:kotless:kotless-lang")
include(":dsl:kotless:kotless-lang-local")
include(":dsl:kotless:kotless-lang-parser")

include(":dsl:spring:spring-boot-lang")
include(":dsl:spring:spring-boot-lang-local")
include(":dsl:spring:spring-lang-parser")

include(":dsl:ktor:ktor-lang")
include(":dsl:ktor:ktor-lang-local")
include(":dsl:ktor:ktor-lang-parser")

include(":dsl:http4k:http4k-lang")
include(":dsl:http4k:http4k-lang-local")
include(":dsl:http4k:http4k-lang-parser")

include(":plugins:gradle")

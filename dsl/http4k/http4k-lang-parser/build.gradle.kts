import io.kotless.buildsrc.Versions
import io.kotless.buildsrc.optInInternalAPI
import tanvd.kosogor.proxy.publishJar

group = rootProject.group
version = rootProject.version

dependencies {
    api(project(":schema"))
    api(project(":dsl:http4k:http4k-lang"))
    api(project(":dsl:common:lang-parser-common"))
    api("org.jetbrains.kotlin", "kotlin-reflect", Versions.kotlin)
}

publishJar {
    bintray {
        username = "tanvd"
        repository = "io.kotless"
        info {
            description = "http4k DSL Parser"
            githubRepo = "https://github.com/JetBrains/kotless"
            vcsUrl = "https://github.com/JetBrains/kotless"
            labels.addAll(listOf("kotlin", "serverless", "web", "devops", "faas", "lambda"))
        }
    }
}

optInInternalAPI()

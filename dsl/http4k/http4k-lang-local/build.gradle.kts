import io.kotless.buildsrc.Versions
import io.kotless.buildsrc.optInInternalAPI
import tanvd.kosogor.proxy.publishJar

group = rootProject.group
version = rootProject.version

dependencies {
    api(project(":dsl:http4k:http4k-lang"))
    api("org.http4k", "http4k-core", Versions.http4k)
    api("org.jetbrains.kotlin", "kotlin-reflect", Versions.kotlin)
}

publishJar {
    bintray {
        username = "tanvd"
        repository = "io.kotless"
        info {
            description = "http4k DSL Local Runner"
            githubRepo = "https://github.com/JetBrains/kotless"
            vcsUrl = "https://github.com/JetBrains/kotless"
            labels.addAll(listOf("kotlin", "serverless", "web", "devops", "faas", "lambda"))
        }
    }
}

optInInternalAPI()

import io.kotless.buildsrc.optInInternalAPI
import tanvd.kosogor.proxy.publishJar

group = rootProject.group
version = rootProject.version

dependencies {
    api(project(":schema"))
    api(project(":dsl:http4k:http4k-lang"))
    api(project(":dsl:common:lang-parser-common"))

    api(project(":dsl:ktor:ktor-lang"))
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

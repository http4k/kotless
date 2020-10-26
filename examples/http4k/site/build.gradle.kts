import io.kotless.plugin.gradle.dsl.Webapp.Route53
import io.kotless.plugin.gradle.dsl.kotless
import io.kotless.resource.Lambda.Config.Runtime

group = rootProject.group
version = rootProject.version


plugins {
    id("io.kotless") version "0.1.7-beta-4" apply true
}

dependencies {
    implementation("io.kotless", "http4k-lang", "0.1.7-beta-4")

    implementation(project(":common:site-shared"))

    implementation("io.kotless", "ktor-lang", "0.1.7-beta-4")
}

kotless {
    config {
        bucket = "eu.http4k-site.s3.ktls.aws.intellij.net"
        prefix = "http4k-site"

        terraform {
            profile = "kotless-jetbrains"
            region = "eu-west-1"
        }
    }

    webapp {
        route53 = Route53("http4k.site", "kotless.io")

        lambda {
            runtime = Runtime.GraalVM
        }
    }
}

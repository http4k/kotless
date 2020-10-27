import io.kotless.plugin.gradle.dsl.Webapp.Route53
import io.kotless.plugin.gradle.dsl.kotless
import io.kotless.resource.Lambda.Config.*

var bob: String = "oi"



group = rootProject.group
version = rootProject.version

plugins {
    id("io.kotless") version "0.1.7-beta-4" apply true
}

dependencies {
    implementation("commons-validator", "commons-validator", "1.6")
    implementation("com.amazonaws", "aws-java-sdk-dynamodb", "1.11.650")

    implementation("io.kotless", "http4k-lang", "0.1.7-beta-4")
    implementation("io.ktor", "ktor-html-builder", "1.3.2")
}

fun prop(propName:String, defaultValue:String) = (if(project.hasProperty(propName)) project.property(propName).toString() else defaultValue)

kotless {
    config {
        bucket = prop("bucket", "eu.http4k-short.s3.ktls.aws.intellij.net")
        prefix = "http4k-short"

        terraform {
            profile = "kotless-jetbrains"
            region = "eu-west-1"
        }
    }

    webapp {
        route53 = Route53(prop("route53Alias", "http4k.short"), prop("route53Zone", "kotless.io"))
    }

    extensions {
        local {
            useAWSEmulation = true
        }

        terraform {
            files {
                add(file("src/main/tf/extensions.tf"))
            }
        }
    }
}


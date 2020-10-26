import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile

group = "io.kotless.examples"
version = "0.1.7-beta-4"

plugins {
    id("tanvd.kosogor") version "1.0.9" apply true
    kotlin("jvm") version "1.3.72" apply false
}

subprojects {
    apply {
        plugin("kotlin")
        plugin("tanvd.kosogor")
    }

    repositories {
        jcenter()
        mavenLocal()
    }

    tasks.withType<KotlinJvmCompile> {
        kotlinOptions {
            jvmTarget = "11"
            languageVersion = "1.3"
            apiVersion = "1.3"
        }
    }
}

package io.kotless.plugin.gradle

import com.kotlin.aws.runtime.dsl.runtime
import io.kotless.DSLType
import io.kotless.parser.LocalParser
import io.kotless.plugin.gradle.dsl.kotless
import io.kotless.plugin.gradle.utils.gradle.Dependencies
import io.kotless.plugin.gradle.utils.gradle.applyPluginSafely
import io.kotless.plugin.gradle.utils.gradle.myImplementation
import io.kotless.plugin.gradle.utils.gradle.myKtSourceSet
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.kotlin.dsl.dependencies

object KotlessRuntimeTasks {
    private val supportGraal = setOf(DSLType.Ktor, DSLType.http4k)

    fun Project.setupGraal() {

        if (supportGraal.contains(kotless.config.dsl.typeOrDefault)) {
            project.logger.warn("GraalVM Runtime can be used only with $supportGraal DSL for now")
            return
        }

        dependencies {
            myImplementation("com.kotlin.aws.runtime", "runtime", "0.1.1")
        }

        applyPluginSafely("io.kcdk")

        runtime {
            handler = LocalParser.parse(project.myKtSourceSet.toSet(), Dependencies.getDependencies(project)).entrypoint.qualifiedName
        }

        afterEvaluate {
            val graalShadowJar = tasks.getByName("buildGraalRuntime") as AbstractArchiveTask
            kotless.config.setArchiveTask(graalShadowJar)
            tasks.getByName("initialize").dependsOn(graalShadowJar)
        }
    }
}

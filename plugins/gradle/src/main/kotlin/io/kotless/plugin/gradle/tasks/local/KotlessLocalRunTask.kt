package io.kotless.plugin.gradle.tasks.local

import io.kotless.Constants
import io.kotless.DSLType
import io.kotless.InternalAPI
import io.kotless.parser.LocalParser
import io.kotless.plugin.gradle.dsl.KotlessDSL
import io.kotless.plugin.gradle.dsl.descriptor
import io.kotless.plugin.gradle.dsl.kotless
import io.kotless.plugin.gradle.utils.gradle.Dependencies
import io.kotless.plugin.gradle.utils.gradle.Groups
import io.kotless.plugin.gradle.utils.gradle.myGetByName
import io.kotless.plugin.gradle.utils.gradle.myKtSourceSet
import io.kotless.plugin.gradle.utils.gradle.myLocal
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.dependencies
import java.io.File

/**
 * KotlessLocal task runs Kotless application locally
 *
 * @see kotless
 *
 * Note: Task is cacheable and will regenerate code only if sources or configuration has changed.
 */
@CacheableTask
internal open class KotlessLocalRunTask : DefaultTask() {

    init {
        group = Groups.kotless
    }

    @get:Input
    val myKotless: KotlessDSL
        get() = project.kotless

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val myAllSources: Set<File>
        get() = project.myKtSourceSet.toSet()

    private val finalizers = ArrayList<() -> Unit>()

    fun onShutDown(vararg finalizer: () -> Unit): KotlessLocalRunTask {
        finalizers.addAll(finalizer)
        return this
    }

    @get:Internal
    lateinit var localstack: LocalStackRunner

    @TaskAction
    @OptIn(InternalAPI::class)
    fun act() = with(project) {
        val dsl = Dependencies.dsl(project)

        require(dsl.isNotEmpty()) { "Cannot find \"kotless-lang\", \"ktor-lang\", \"http4l-lang\" or \"spring-boot-lang\" dependencies. One of them required for local start." }
        require(dsl.size <= 1) { "Only one dependency should be used for DSL: either \"kotless-lang\", \"ktor-lang\", \"http4k-lang\" or \"spring-boot-lang\"." }

        val (type, dependency) = dsl.entries.single()

        dependencies {
            myLocal("io.kotless", type.descriptor.localLibrary, dependency.version ?: error("Explicit version is required for Kotless DSL dependency."))
        }

        val run = tasks.myGetByName<JavaExec>("run").apply {
            classpath += files(myLocal().files)

            environment[Constants.Local.serverPort] = myKotless.extensions.local.port

            if (setOf(DSLType.Ktor, DSLType.http4k, DSLType.SpringBoot).contains(type)) {
                val local = LocalParser.parse(myAllSources, Dependencies.getDependencies(project))
                environment[Constants.Local.KtorOrSpringOrHttp4k.classToStart] = local.entrypoint.qualifiedName.substringBefore("::")
            }

            if (type == DSLType.Kotless) {
                environment[Constants.Local.Kotless.workingDir] = myKotless.config.dsl.resolvedStaticsRoot.canonicalPath
            }

            if (myKotless.config.optimization.autowarm.enable) {
                environment[Constants.Local.autowarmMinutes] = myKotless.config.optimization.autowarm.minutes
            }

            for ((key, value) in myKotless.webapp.lambda.mergedEnvironment) {
                environment[key] = value
            }

            if (myKotless.extensions.local.useAWSEmulation) {
                environment.putAll(localstack.environment)
            }

            isIgnoreExitValue = true
        }

        try {
            run.exec()
        } catch (e: Throwable) {
            logger.lifecycle("Gracefully shutting down Kotless local")
            //Remove interrupted flag before execution of finalizers
            Thread.interrupted()
            finalizers.forEach { it.invoke() }
            //Rethrow exception after finalizers executed
            throw e
        }
    }
}

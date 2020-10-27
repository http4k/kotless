package io.kotless.parser.http4k

import io.kotless.Application
import io.kotless.AwsResource
import io.kotless.HttpMethod
import io.kotless.PermissionLevel
import io.kotless.URIPath
import io.kotless.parser.Parser
import io.kotless.parser.processor.ProcessorContext
import io.kotless.permission.Permission
import io.kotless.resource.Lambda
import io.kotless.utils.TypedStorage
import java.io.File

/**
 * Http4kDslParser parses Kotlin code with Kotlin embeddable compiler looking
 * for Http4k DSL constructs.
 *
 * The result of parsing is a number of Lambdas and StaticResources and associated
 * with them Dynamic and Static routes
 */
object Http4kParser : Parser(setOf()) {
    override fun processResources(resources: Set<File>, context: ProcessorContext) {

        val key = TypedStorage.Key<Lambda>()
        val function = Lambda("http4-kotless-function", context.jar, Lambda.Entrypoint("io.kotless.examples.Server::handleRequest"), context.lambda, setOf(Permission(AwsResource.CloudWatchLogs, PermissionLevel.ReadWrite, setOf("*"))))

        context.resources.register(key, function)

        context.routes.register(Application.ApiGateway.DynamicRoute(HttpMethod.GET, URIPath("/hello"), key))
    }
}

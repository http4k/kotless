package io.kotless.parser.http4k

import io.kotless.parser.Parser
import io.kotless.parser.http4k.processor.action.GlobalActionsProcessor
import io.kotless.parser.http4k.processor.route.DynamicRoutesProcessor
import io.kotless.parser.http4k.processor.route.StaticRoutesProcessor
import io.kotless.parser.processor.config.EntrypointProcessor

/**
 * Http4kDslParser parses Kotlin code with Kotlin embeddable compiler looking
 * for Http4k DSL constructs.
 *
 * The result of parsing is a number of Lambdas and StaticResources and associated
 * with them Dynamic and Static routes
 */
object Http4kParser : Parser(setOf(EntrypointProcessor, GlobalActionsProcessor, DynamicRoutesProcessor, StaticRoutesProcessor))
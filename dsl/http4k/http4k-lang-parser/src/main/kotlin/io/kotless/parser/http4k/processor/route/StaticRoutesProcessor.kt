package io.kotless.parser.http4k.processor.route

import io.kotless.Application
import io.kotless.MimeType
import io.kotless.URIPath
import io.kotless.dsl.http4k.Kotless
import io.kotless.parser.processor.ProcessorContext
import io.kotless.parser.processor.SubTypesProcessor
import io.kotless.parser.utils.errors.error
import io.kotless.parser.utils.errors.require
import io.kotless.parser.utils.psi.asString
import io.kotless.parser.utils.psi.getArgument
import io.kotless.parser.utils.psi.getArgumentByIndexOrNull
import io.kotless.parser.utils.psi.getArgumentOrNull
import io.kotless.parser.utils.psi.getChildAt
import io.kotless.parser.utils.psi.getFqName
import io.kotless.parser.utils.psi.visitBinaryExpressions
import io.kotless.parser.utils.psi.visitCallExpressionsWithReferences
import io.kotless.parser.utils.psi.visitNamedFunctions
import io.kotless.parser.utils.psi.visitor.KtReferenceFollowingVisitor
import io.kotless.parser.utils.reversed
import io.kotless.resource.StaticResource
import io.kotless.toURIPath
import io.kotless.utils.TypedStorage
import org.http4k.core.MimeTypes
import org.jetbrains.kotlin.lexer.KtSingleValueToken
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.resolve.BindingContext
import java.io.File

internal object StaticRoutesProcessor : SubTypesProcessor<Unit>() {
    private val functions = setOf(
        "io.ktor.http.content.file",
        "io.ktor.http.content.files",
        "io.ktor.http.content.default"
    )

    override val klasses = setOf(Kotless::class)

    override fun mayRun(context: ProcessorContext) = true

    override fun process(files: Set<KtFile>, binding: BindingContext, context: ProcessorContext) {
        processClassesOrObjects(files, binding) { klass, _ ->
            klass.visitNamedFunctions(filter = { func -> func.name == Kotless::handler.name }) { func ->
                func.visitCallExpressionsWithReferences(filter = { it.getFqName(binding) in functions }, binding = binding) { element ->
                    val outer = getStaticPath(element, binding)
                    val base = getStaticRootFolder(element, binding, context)

                    when (element.getFqName(binding)) {
                        "io.ktor.http.content.file" -> {
                            val remotePath = element.getArgument("remotePath", binding).asString(binding)
                            val localPath = element.getArgumentOrNull("localPath", binding)?.asString(binding) ?: remotePath

                            val file = File(base, localPath)
                            val path = URIPath(outer, remotePath)

                            createResource(file, path, context)
                        }
                        "io.ktor.http.content.default" -> {
                            val localPath = element.getArgument("localPath", binding).asString(binding)

                            val file = File(base, localPath)

                            createResource(file, outer, context)
                        }
                        "io.ktor.http.content.files" -> {
                            val folder = File(base, element.getArgument("folder", binding).asString(binding))

                            addStaticFolder(folder, outer, context)
                        }
                    }
                }
            }
        }
    }

    private fun addStaticFolder(folder: File, outer: URIPath, context: ProcessorContext) {
        val allFiles = folder.listFiles() ?: return

        for (file in allFiles) {
            when {
                file.isDirectory -> addStaticFolder(file, URIPath(outer, file.name), context)
                file.isFile -> {
                    val remotePath = file.toRelativeString(folder).toURIPath()
                    val path = URIPath(outer, remotePath)

                    createResource(file, path, context)
                }
            }
        }
    }

    private fun KtReferenceFollowingVisitor.getStaticPath(element: KtElement, binding: BindingContext): URIPath {
        val calls = element.parentsWithReferences(KtCallExpression::class) { it.getFqName(binding) == "io.ktor.http.content.static" }

        val path = calls.mapNotNull {
            it.getArgumentOrNull("remotePath", binding)?.asString(binding)
        }.reversed().toList()

        return URIPath(path)
    }

    private fun KtReferenceFollowingVisitor.getStaticRootFolder(element: KtElement, binding: BindingContext, context: ProcessorContext): File {
        val previous = element.parentsWithReferences(KtCallExpression::class) { it.getFqName(binding) == "io.ktor.http.content.static" }

        return previous.mapNotNull { static ->
            var folder: File? = null
            static.visitCallExpressionsWithReferences(filter = { el -> el.getFqName(binding) == "io.ktor.http.content.static" }, binding = binding) { call ->
                call.visitBinaryExpressions(filter = { (it.operationToken as? KtSingleValueToken)?.value == "=" }) { binary ->
                    if (binary.getChildAt<KtNameReferenceExpression>(0)?.getFqName(binding) != "io.ktor.http.content.staticRootFolder") {
                        return@visitBinaryExpressions
                    }

                    val right = binary.getChildAt<KtCallExpression>(2)
                        ?: error(binary, "staticRootFolder should be assigned with java.io.File(...) constructor")

                    require(binary, right.getFqName(binding) == "java.io.File.<init>") {
                        "staticRootFolder should be assigned with java.io.File(...) constructor"
                    }

                    right.getArgumentByIndexOrNull(0)?.asString(binding)?.let { value ->
                        folder = if (!value.startsWith("/")) {
                            File(context.config.dsl.staticsRoot, value)
                        } else {
                            File(value)
                        }
                    }
                }
            }
            folder
        }.firstOrNull() ?: context.config.dsl.staticsRoot
    }

    private fun createResource(file: File, path: URIPath, context: ProcessorContext) {
        val mime = MimeType.forFile(file)
            ?: MimeTypes().forFile(file.name).value.split("/")
                .let { parts ->
                    MimeType.values()
                        .find { it.mimeText == "${parts[0]}/${parts[1]}" }
                }
        require(mime != null) { "Unknown mime type for file $file" }

        val resource = StaticResource(URIPath("static", path), file, mime)

        val key = TypedStorage.Key<StaticResource>()

        context.resources.register(key, resource)
        context.routes.register(Application.ApiGateway.StaticRoute(path, key))
    }
}

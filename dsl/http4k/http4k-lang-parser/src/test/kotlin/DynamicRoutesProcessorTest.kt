import io.kotless.DSLType
import io.kotless.KotlessConfig
import io.kotless.parser.http4k.processor.route.DynamicRoutesProcessor
import io.kotless.parser.processor.ProcessorContext
import io.kotless.parser.utils.psi.analysis.EnvironmentManager
import io.kotless.parser.utils.psi.analysis.ParseUtil
import io.kotless.parser.utils.psi.analysis.ResolveUtil
import io.kotless.resource.Lambda
import org.gradle.internal.impldep.org.apache.commons.io.FileUtils
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class DynamicRoutesProcessorTest {

    @Rule
    @JvmField
    val testProjectDir = TemporaryFolder()

    @Before
    fun setUpProject() {
        testProjectDir.create()
        writeResourceTo("/build.gradle", testProjectDir.root)
        writeResourceTo("/MyFunction.kt", testProjectDir.root)
    }

    @Test
    fun `build routes from Kotless app code`() {
        val files = setOf(File(testProjectDir.root, "MyFunction.kt"))

        val builder = ProjectBuilder.builder().withProjectDir(testProjectDir.root)
        val dependencies = with(builder.build()) {
            configurations.getByName("allDependencies").files.toSet()
        }

        val ktFiles = ParseUtil.analyze(files, EnvironmentManager.create(dependencies))
        val binding = ResolveUtil.analyze(ktFiles, EnvironmentManager.create(dependencies)).bindingContext

        val context = ProcessorContext(File(""), KotlessConfig("", "", KotlessConfig.DSL(DSLType.http4k, File("")), KotlessConfig.Terraform("", KotlessConfig.Terraform.Backend("", "", "", ""), KotlessConfig.Terraform.AWSProvider("", "", ""))), Lambda.Config(1, 1, Lambda.Config.Runtime.Java11, emptyMap()))

        DynamicRoutesProcessor.run(ktFiles, binding, context)

        println(context)
    }

    private fun writeResourceTo(resource: String, dir: File) {
        dir.mkdirs()
        FileUtils.copyFileToDirectory(File(javaClass.getResource(resource).file), dir)
    }

}


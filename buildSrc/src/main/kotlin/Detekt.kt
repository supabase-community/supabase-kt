import io.gitlab.arturbosch.detekt.DetektPlugin
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import io.gitlab.arturbosch.detekt.report.ReportMergeTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.findPlugin
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.withType

fun Project.applyDetektWithConfiguration(reportMerge: TaskProvider<ReportMergeTask>) {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    plugins.findPlugin(DetektPlugin::class)?.let {
        extensions.configure(DetektExtension::class) {
            buildUponDefaultConfig = true
            config.setFrom(files("$rootDir/detekt.yml"))
            //baseline = file("$rootDir/config/detekt/baseline.xml")
        }
    }

    tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
        jvmTarget = "1.8"
        reports {
            xml.required.set(false)
            html.required.set(false)
            txt.required.set(false)
            sarif.required.set(true)
            md.required.set(false)
        }
        basePath = rootDir.absolutePath
    }

    tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
        finalizedBy(reportMerge)
    }

    reportMerge.invoke {
        input.from(tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().map { it.sarifReportFile })
    }

}
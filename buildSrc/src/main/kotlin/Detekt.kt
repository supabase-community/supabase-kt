import io.gitlab.arturbosch.detekt.DetektPlugin
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.findPlugin
import org.gradle.kotlin.dsl.withType

fun Project.applyDetektWithConfiguration() {
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
            xml.required.set(true)
            html.required.set(true)
            txt.required.set(true)
            sarif.required.set(true)
            md.required.set(true)
        }
        basePath = rootDir.absolutePath
    }
}
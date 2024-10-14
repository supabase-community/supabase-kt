import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.powerassert.gradle.PowerAssertGradleExtension

@OptIn(ExperimentalKotlinGradlePluginApi::class)
fun Project.applyPowerAssertConfiguration() {
    apply(plugin = "org.jetbrains.kotlin.plugin.power-assert")

    extensions.configure<PowerAssertGradleExtension>(PowerAssertGradleExtension::class.java) {
        functions.addAll(
            listOf("kotlin.assert", "kotlin.test.assertTrue", "kotlin.test.assertEquals",
                "kotlin.test.assertNull", "kotlin.test.assertIs", "kotlin.test.assertContentContains",
                "kotlin.test.assertContains")
        )
    }
}
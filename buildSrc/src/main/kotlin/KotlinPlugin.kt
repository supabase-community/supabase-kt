import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

@OptIn(ExperimentalKotlinGradlePluginApi::class)
fun KotlinMultiplatformExtension.defaultConfig() {
    sourceSets.all {
        languageSettings.optIn("kotlin.RequiresOptIn")
        languageSettings.optIn("io.github.jan.supabase.annotations.SupabaseInternal")
        languageSettings.optIn("io.github.jan.supabase.annotations.SupabaseExperimental")
        languageSettings.optIn("kotlin.time.ExperimentalTime")
        languageSettings.optIn("kotlin.concurrent.atomics.ExperimentalAtomicApi")
    }
    compilerOptions.freeCompilerArgs.add("-Xexpect-actual-classes")
    applyDefaultHierarchyTemplate()
 //   jvmToolchain(8)
}
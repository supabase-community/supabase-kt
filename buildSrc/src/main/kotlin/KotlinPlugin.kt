import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

@OptIn(ExperimentalKotlinGradlePluginApi::class)
fun KotlinMultiplatformExtension.defaultConfig() {
    sourceSets.all {
        languageSettings.optIn("kotlin.RequiresOptIn")
        languageSettings.optIn("io.supabase.annotations.SupabaseInternal")
        languageSettings.optIn("io.supabase.annotations.SupabaseExperimental")
    }
    compilerOptions.freeCompilerArgs.add("-Xexpect-actual-classes")
    applyDefaultHierarchyTemplate()
 //   jvmToolchain(8)
}
import org.jetbrains.compose.desktop.DesktopExtension
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler

enum class SupabaseModule(val module: String, val extern: Boolean = false) {
    SUPABASE("supabase-kt"),
    AUTH("auth-kt"),
    STORAGE("storage-kt"),
    REALTIME("realtime-kt"),
    FUNCTIONS("functions-kt"),
    POSTGREST("postgrest-kt"),
}

fun KotlinDependencyHandler.addModules(vararg modules: SupabaseModule) {
    modules.forEach {
        api(project(":${it.module}"))
    }
}

fun DesktopExtension.configureComposeDesktop(
    name: String,
) {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = name
            packageVersion = "1.0.0"
        }
    }
}
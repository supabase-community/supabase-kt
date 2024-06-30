import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler

enum class SupabaseModule(val module: String) {
    GOTRUE("gotrue-kt"),
    STORAGE("storage-kt"),
    REALTIME("realtime-kt"),
    FUNCTIONS("functions-kt"),
    POSTGREST("postgrest-kt"),
    COMPOSE_AUTH("plugins:compose-auth"),
    COMPOSE_AUTH_UI("plugins:compose-auth-ui"),
}

fun KotlinDependencyHandler.addModules(vararg modules: SupabaseModule) {
    modules.forEach {
        api(project(":${it.module}"))
    }
}
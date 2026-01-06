import io.github.jan.supabase.auth.AuthConfig

actual fun AuthConfig.platformSettings() {
    enableLifecycleCallbacks = false
}
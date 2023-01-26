import io.github.jan.supabase.gotrue.GoTrueConfig

actual fun GoTrueConfig.platformSettings() {
    enableLifecycleCallbacks = false
}
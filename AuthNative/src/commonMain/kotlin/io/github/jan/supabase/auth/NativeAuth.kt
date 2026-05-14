package io.github.jan.supabase.auth

private val AuthConfig.isInitialized get() = nativeAuthConfig is PlatformNativeAuthConfig

fun AuthConfig.initializeNativeAuth() {
    if(isInitialized) return
    nativeAuthConfig = PlatformNativeAuthConfig()
}
package io.github.jan.supabase.auth.native.native

import io.github.jan.supabase.auth.AuthConfig

private val AuthConfig.isInitialized get() = nativeAuthConfig is PlatformNativeAuthConfig

fun AuthConfig.initializeNativeAuth() {
    if(isInitialized) return
    nativeAuthConfig = PlatformNativeAuthConfig()
}
package io.github.jan.supabase.auth.native

import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.AuthConfig
import io.github.jan.supabase.auth.NativeAuthConfig

abstract class DefaultNativeAuthConfig: NativeAuthConfig {

    var urlLauncher = UrlLauncher.DEFAULT

    override fun defaultRedirectUrl(auth: Auth): String? {
        return auth.defaultPlatformRedirectUrl()
    }

    override suspend fun setupNativePlatform(auth: Auth) {
        return auth.setupNativePlatform()
    }

}

expect class PlatformNativeAuthConfig(): DefaultNativeAuthConfig

@SupabaseInternal fun AuthConfig.platformConfigOrNull(): PlatformNativeAuthConfig? = this.nativeAuthConfig as? PlatformNativeAuthConfig

@SupabaseInternal fun AuthConfig.platformConfig() = platformConfigOrNull() ?: error("Native Auth not initialized")
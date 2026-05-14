package io.github.jan.supabase.auth

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
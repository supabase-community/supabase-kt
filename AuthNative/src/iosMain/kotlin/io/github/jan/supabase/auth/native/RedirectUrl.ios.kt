package io.github.jan.supabase.auth.native.native

import io.github.jan.supabase.auth.Auth

internal actual fun Auth.defaultPlatformRedirectUrl(): String? {
    return config.platformConfig()?.let { config ->
        config.appHost?.let { "$it://${config.appHost ?: ""}" }
    }
}
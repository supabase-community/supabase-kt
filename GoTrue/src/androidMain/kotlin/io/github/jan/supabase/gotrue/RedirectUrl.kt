package io.github.jan.supabase.gotrue

import io.github.jan.supabase.annotations.SupabaseInternal

@SupabaseInternal
actual fun Auth.generateRedirectUrl(fallbackUrl: String?): String? {
    if(fallbackUrl != null) return fallbackUrl
    val scheme = config.scheme ?: return null
    val host = config.host ?: return null
    this as AuthImpl
    return "${scheme}://${host}"
}

internal val AuthConfig.deepLink: String
    get() {
        val scheme = scheme ?: noDeeplinkError("scheme")
        val host = host ?: noDeeplinkError("host")
        return "${scheme}://${host}"
    }
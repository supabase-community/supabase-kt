package io.github.jan.supabase.gotrue

import io.github.jan.supabase.annotations.SupabaseInternal

@SupabaseInternal
actual fun GoTrue.generateRedirectUrl(fallbackUrl: String?): String? {
    if(fallbackUrl != null) return fallbackUrl
    val scheme = config.scheme ?: return null
    val host = config.host ?: return null
    this as GoTrueImpl
    return "${scheme}://${host}"
}

internal val GoTrueConfig.deepLink: String
    get() {
        val scheme = scheme ?: noDeeplinkError("scheme")
        val host = host ?: noDeeplinkError("host")
        return "${scheme}://${host}"
    }
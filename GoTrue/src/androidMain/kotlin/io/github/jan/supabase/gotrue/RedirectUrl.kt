package io.github.jan.supabase.gotrue

import io.github.jan.supabase.annotations.SupabaseInternal

@SupabaseInternal
actual fun Auth.defaultRedirectUrl(): String? = config.deepLinkOrNull

internal val AuthConfig.deepLink: String
    get() {
        val scheme = scheme ?: noDeeplinkError("scheme")
        val host = host ?: noDeeplinkError("host")
        return "${scheme}://${host}"
    }

internal val AuthConfig.deepLinkOrNull: String?
    get() {
        val scheme = scheme ?: return null
        val host = host ?: return null
        return "${scheme}://${host}"
    }
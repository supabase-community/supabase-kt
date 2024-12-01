package io.supabase.auth

import io.supabase.annotations.SupabaseInternal

@SupabaseInternal
internal expect fun Auth.defaultPlatformRedirectUrl(): String?

internal fun Auth.defaultRedirectUrl(): String? {
    return config.defaultRedirectUrl ?: defaultPlatformRedirectUrl()
}
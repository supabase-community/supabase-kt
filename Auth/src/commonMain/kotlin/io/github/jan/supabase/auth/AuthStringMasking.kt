package io.github.jan.supabase.auth

import io.github.jan.supabase.StringMasking
import io.github.jan.supabase.auth.user.UserSession

internal fun StringMasking.maskSession(value: UserSession): UserSession {
    return value.copy(
        accessToken = maskString(value.accessToken, showLength = true),
        refreshToken = maskString(value.refreshToken, showLength = true),
        providerRefreshToken = value.providerRefreshToken?.let { maskString(it, showLength = true) },
        providerToken = value.providerToken?.let { maskString(it, showLength = true) }
    )
}
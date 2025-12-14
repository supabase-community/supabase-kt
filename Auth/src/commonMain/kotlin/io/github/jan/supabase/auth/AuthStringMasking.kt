package io.github.jan.supabase.auth

import io.github.jan.supabase.StringMasking
import io.github.jan.supabase.auth.user.UserSession

internal fun StringMasking.maskSession(value: UserSession): UserSession {
    return value.copy(
        accessToken = maskString(value.accessToken, 3, showLength = true),
        refreshToken = maskString(value.refreshToken, 3, showLength = true),
        providerRefreshToken = value.providerRefreshToken?.let { maskString(it, 3, true) },
        providerToken = value.providerToken?.let { maskString(it, 3, true) }
    )
}
package io.github.jan.supabase.auth

import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession

data class AuthResponse(
    val user: UserInfo? = null,
    val session: UserSession? = null
)
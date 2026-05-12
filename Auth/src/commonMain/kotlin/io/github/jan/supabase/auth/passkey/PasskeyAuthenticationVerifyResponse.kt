package io.github.jan.supabase.auth.passkey

import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.serialization.Serializable

@Serializable
data class PasskeyAuthenticationVerifyResponse(
    val session: UserSession? = null,
    val user: UserInfo? = null
)

package io.github.jan.supabase.auth.passkey

import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.serialization.Serializable

/**
 * Response for [AuthPasskeyApi.verifyRegistration]
 * @param session The session received
 * @param user The user received
 */
@Serializable
data class PasskeyAuthenticationVerifyResponse(
    val session: UserSession? = null,
    val user: UserInfo? = null
)

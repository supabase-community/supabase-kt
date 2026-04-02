package io.github.jan.supabase.auth.oauth

/**
 * Information about the [OAuthAuthorizationUser]
 * @param id User ID (UUID)
 * @param email User email
 */
data class OAuthAuthorizationUser(
    val id: String,
    val email: String
)

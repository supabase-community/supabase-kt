package io.github.jan.supabase.gotrue.mfa

/**
 * The assurance level of a session
 */
enum class AuthenticatorAssuranceLevel {
    /**
     * The user is logged in using a provider (Password, OAuth, etc.)
     */
    AAL1,

    /**
     * The user is logged in using at least one MFA factor
     */
    AAL2;

    companion object {

        fun from(value: String): AuthenticatorAssuranceLevel {
            return when (value) {
                "aal1" -> AAL1
                "aal2" -> AAL2
                else -> throw IllegalArgumentException("Unknown AuthenticatorAssuranceLevel: $value")
            }
        }

    }
}

/**
 * @param current The current assurance level of the session
 * @param next The next possible assurance level (for the next login)
 */
data class MfaLevel(
    val current: AuthenticatorAssuranceLevel,
    val next: AuthenticatorAssuranceLevel
)
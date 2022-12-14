package io.github.jan.supabase.gotrue.mfa

enum class AuthenticatorAssuranceLevel {
    AAL1, AAL2;

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

data class MfaLevel(
    val current: AuthenticatorAssuranceLevel,
    val next: AuthenticatorAssuranceLevel
)
package io.github.jan.supabase.auth

import io.github.jan.supabase.auth.mfa.MfaApi
import kotlin.time.Instant


/**
 * An authentication method reference (AMR) entry.
 *
 * An entry designates what method was used by the user to verify their
 * identity and at what time.
 *
 * Note: Custom access token hooks can return AMR claims as either:
 * - An array of AMREntry objects (detailed format with timestamps)
 * - An array of strings (RFC-8176 compliant format)
 *
 * @param method Authentication method name.
 * @param timestamp Timestamp when the method was successfully used.
 *
 * @see [MfaApi.getAuthenticatorAssuranceLevel].
 */
data class AMREntry(
    val method: AMRMethod,
    val timestamp: Instant
)

/**
 * Represents an AMR Method.
 * @param value The string name of this method
 */
sealed class AMRMethod(val value: String) {
    data object Password : AMRMethod("password")
    data object OTP : AMRMethod("otp")
    data object OAuth : AMRMethod("oauth")
    data object TOTP: AMRMethod("totp")
    data object MFA {
        data object TOTP: AMRMethod("mfa/totp")
        data object Phone: AMRMethod("mfa/phone")
        data object WebAuthn: AMRMethod("mfa/webauthn")
    }
    data object Anonymous: AMRMethod("anonymous")
    data object SSO {
        data object SAML: AMRMethod("sso/saml")
    }
    data object MagicLink: AMRMethod("magiclinl")
    data object Web3: AMRMethod("web3")
}
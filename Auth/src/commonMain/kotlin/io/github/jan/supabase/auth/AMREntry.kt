package io.github.jan.supabase.auth

import kotlin.time.Instant

data class AMREntry(
    val method: AMRMethod,
    val timestamp: Instant
)

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
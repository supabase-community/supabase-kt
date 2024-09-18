@file:Suppress("UndocumentedPublicProperty")
package io.github.jan.supabase.auth.exception

/**
 * Enum class for error codes returned by the Auth API.
 * @param value The actual error code returned by the API. Equivalent to the [AuthRestException.error] property.
 */
enum class AuthErrorCode(val value: String) {
    UnexpectedFailure("unexpected_failure"),
    ValidationFailed("validation_failed"),
    BadJson("bad_json"),
    EmailExists("email_exists"),
    PhoneExists("phone_exists"),
    BadJwt("bad_jwt"),
    NotAdmin("not_admin"),
    NoAuthorization("no_authorization"),
    UserNotFound("user_not_found"),
    SessionNotFound("session_not_found"),
    FlowStateNotFound("flow_state_not_found"),
    FlowStateExpired("flow_state_expired"),
    SignupDisabled("signup_disabled"),
    UserBanned("user_banned"),
    ProviderEmailNeedsVerification("provider_email_needs_verification"),
    InviteNotFound("invite_not_found"),
    BadOauthState("bad_oauth_state"),
    BadOauthCallback("bad_oauth_callback"),
    OauthProviderNotSupported("oauth_provider_not_supported"),
    UnexpectedAudience("unexpected_audience"),
    SingleIdentityNotDeletable("single_identity_not_deletable"),
    EmailConflictIdentityNotDeletable("email_conflict_identity_not_deletable"),
    IdentityAlreadyExists("identity_already_exists"),
    EmailProviderDisabled("email_provider_disabled"),
    PhoneProviderDisabled("phone_provider_disabled"),
    TooManyEnrolledMfaFactors("too_many_enrolled_mfa_factors"),
    MfaFactorNameConflict("mfa_factor_name_conflict"),
    MfaFactorNotFound("mfa_factor_not_found"),
    MfaIpAddressMismatch("mfa_ip_address_mismatch"),
    MfaChallengeExpired("mfa_challenge_expired"),
    MfaVerificationFailed("mfa_verification_failed"),
    MfaVerificationRejected("mfa_verification_rejected"),
    InsufficientAal("insufficient_aal"),
    CaptchaFailed("captcha_failed"),
    SamlProviderDisabled("saml_provider_disabled"),
    ManualLinkingDisabled("manual_linking_disabled"),
    SmsSendFailed("sms_send_failed"),
    EmailNotConfirmed("email_not_confirmed"),
    PhoneNotConfirmed("phone_not_confirmed"),
    ReauthNonceMissing("reauth_nonce_missing"),
    SamlRelayStateNotFound("saml_relay_state_not_found"),
    SamlRelayStateExpired("saml_relay_state_expired"),
    SamlIdpNotFound("saml_idp_not_found"),
    SamlAssertionNoUserId("saml_assertion_no_user_id"),
    SamlAssertionNoEmail("saml_assertion_no_email"),
    UserAlreadyExists("user_already_exists"),
    SsoProviderNotFound("sso_provider_not_found"),
    SamlMetadataFetchFailed("saml_metadata_fetch_failed"),
    SamlIdpAlreadyExists("saml_idp_already_exists"),
    SsoDomainAlreadyExists("sso_domain_already_exists"),
    SamlEntityIdMismatch("saml_entity_id_mismatch"),
    Conflict("conflict"),
    ProviderDisabled("provider_disabled"),
    UserSsoManaged("user_sso_managed"),
    ReauthenticationNeeded("reauthentication_needed"),
    SamePassword("same_password"),
    ReauthenticationNotValid("reauthentication_not_valid"),
    OtpExpired("otp_expired"),
    OtpDisabled("otp_disabled"),
    IdentityNotFound("identity_not_found"),
    WeakPassword("weak_password"),
    OverRequestRateLimit("over_request_rate_limit"),
    OverEmailSendRateLimit("over_email_send_rate_limit"),
    OverSmsSendRateLimit("over_sms_send_rate_limit"),
    BadCodeVerifier("bad_code_verifier");

    companion object {
        /**
         * Returns the [AuthErrorCode] for the given [value]. If the [value] is not a known error code, null is returned.
         */
        fun fromValue(value: String): AuthErrorCode? {
            return entries.firstOrNull { it.value == value }
        }
    }
}
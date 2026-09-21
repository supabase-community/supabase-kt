package io.github.jan.supabase.auth

/**
 * Config for [Auth.resetPasswordForEmail]
 */
class ResetPasswordConfig {

    /**
     * Verification token received when the user completes the captcha on the site.
     */
    var captchaToken: String? = null

    /**
     * A URL to send the user to after they are confirmed. Defaults to [Auth.defaultRedirectUrl], if null.
     */
    var redirectUrl: String? = null

}
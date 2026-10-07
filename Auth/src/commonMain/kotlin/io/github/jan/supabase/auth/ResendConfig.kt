package io.github.jan.supabase.auth

/**
 * Base config for [Auth.resend]
 * @see Email
 * @see Phone
 */
open class ResendConfig {

    /**
     * Verification token received when the user completes the captcha on the site.
     */
    var captchaToken: String? = null

    /**
     * Config for [Auth.resend]
     */
    class Email: ResendConfig() {

        /**
         * A URL to send the user to after they are confirmed. Defaults to [Auth.defaultRedirectUrl], if null.
         */
        var redirectUrl: String? = null

    }

    /**
     * Config for [Auth.resend]
     */
    class Phone: ResendConfig()

}
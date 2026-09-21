package io.github.jan.supabase.auth

import kotlinx.serialization.json.JsonObject

/**
 * Config for [Auth.signInAnonymously]
 */
class AnonymousSignInConfig {

    /**
     * Extra data to create the user with
     */
    var data: JsonObject? = null

    /**
     * Verification token received when the user completes the captcha on the site.
     */
    var captchaToken: String? = null

}
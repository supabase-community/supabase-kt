package io.github.jan.supabase.auth

/**
 * The configuration for [Auth]
 */
actual class AuthConfig : AuthConfigDefaults() {

    /**
     * App scheme used for OAuth and magic link handling.
     */
    @Suppress("DEPRECATION")
    var appScheme: String?
        get() = scheme
        set(value) {
            scheme = value
        }

}
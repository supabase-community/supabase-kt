package io.github.jan.supabase.auth

import io.github.jan.supabase.auth.providers.ExternalAuthConfig

/**
 * The configuration for [Auth]
 */
actual class AuthConfig : AuthConfigDefaults() {

    /**
     * The action to use for the OAuth flow. Can be overriden per-request in the [ExternalAuthConfig]
     */
    var defaultExternalAuthAction: ExternalAuthAction = ExternalAuthAction.DEFAULT

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
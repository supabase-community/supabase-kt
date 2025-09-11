package io.github.jan.supabase.auth

import io.github.jan.supabase.plugins.CustomSerializationConfig

/**
 * The configuration for [Auth]
 */
actual class AuthConfig: CustomSerializationConfig, AuthConfigDefaults() {

    /**
     * Whether to disable automatic URL checking for PKCE codes, error codes, and session tokens.
     */
    var disableUrlChecking: Boolean = false

}
package io.github.jan.supabase.auth

/**
 * The configuration for [Auth] in web applications.
 */
open class WebAuthConfig: AuthConfigDefaults() {

    /**
     * Whether to disable automatic URL checking for PKCE codes, error codes, and session tokens.
     */
    var disableUrlChecking: Boolean = false


}
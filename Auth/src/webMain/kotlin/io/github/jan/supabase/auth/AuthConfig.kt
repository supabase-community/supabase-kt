package io.github.jan.supabase.auth

import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.plugins.CustomSerializationConfig
import io.ktor.util.PlatformUtils.IS_BROWSER

/**
 * The configuration for [Auth]
 */
actual class AuthConfig: CustomSerializationConfig, AuthConfigDefaults() {

    /**
     * Whether to disable automatic URL checking for PKCE codes, error codes, and session tokens.
     */
    var disableUrlChecking: Boolean = false

    /**
     * Interface to access browser properties like the current hash. By default, null on NodeJS. Can be changed for testing.
     */
    @SupabaseInternal
    var browserBridge: BrowserBridge? = if(IS_BROWSER) BrowserBridgeImpl() else null

}
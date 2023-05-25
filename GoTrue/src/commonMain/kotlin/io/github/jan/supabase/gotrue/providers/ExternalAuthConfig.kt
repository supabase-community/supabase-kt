package io.github.jan.supabase.gotrue.providers

/**
 * Configuration for external authentication providers like Google, Twitter, etc.
 */
expect class ExternalAuthConfig: ExternalAuthConfigDefaults

/**
 * The default values for [ExternalAuthConfig]
 */
open class ExternalAuthConfigDefaults {

    /**
     * The scopes to request from the external provider
     */
    val scopes = mutableListOf<String>()

    /**
     * Additional query parameters to send to the external provider
     */
    val queryParams = mutableMapOf<String, String>()

}
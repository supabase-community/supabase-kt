package io.github.jan.supabase.auth

/**
 * Configuration for external authentication providers like Google, Twitter, etc.
 */
open class OAuthConfig {

    /**
     * The scopes to request from the external provider
     */
    val scopes = mutableListOf<String>()

    /**
     * Additional query parameters to send to the external provider
     */
    val queryParams = mutableMapOf<String, String>()

    var redirectUrl: String? = null

}
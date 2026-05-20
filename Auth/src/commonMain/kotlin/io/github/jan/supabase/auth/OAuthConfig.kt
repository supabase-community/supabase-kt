package io.github.jan.supabase.auth

interface OAuthConfig {

    val scopes: MutableList<String>
    val queryParams: MutableMap<String, String>
    var redirectUrl: String?

}

/**
 * Configuration for external authentication providers like Google, Twitter, etc.
 */
open class DefaultOAuthConfig: OAuthConfig {

    /**
     * The scopes to request from the external provider
     */
    override val scopes = mutableListOf<String>()

    /**
     * Additional query parameters to send to the external provider
     */
    override val queryParams = mutableMapOf<String, String>()

    override var redirectUrl: String? = null

}
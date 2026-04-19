package io.github.jan.supabase.auth.admin.custom.provider

/**
 * Contains all custom OIDC/OAuth provider administration methods.
 */
interface CustomProvidersApi {

    /**
     * Lists all custom providers with optional type filter.
     *
     * This function should only be called on a server. Never expose your `service_role` key in the browser.
     */
    suspend fun listProviders()

    /**
     * Creates a new custom OIDC/OAuth provider.
     *
     * For OIDC providers, the server fetches and validates the OpenID Connect discovery document
     * from the issuer's well-known endpoint (or the provided `discovery_url`) at creation time.
     * This may return a validation error (`error_code: "validation_failed"`) if the discovery
     * document is unreachable, not valid JSON, missing required fields, or if the issuer
     * in the document does not match the expected issuer.
     *
     * This function should only be called on a server. Never expose your `service_role` key in the browser.
     */
    suspend fun createProvider()

    /**
     * Gets details of a specific custom provider by identifier.
     *
     * This function should only be called on a server. Never expose your `service_role` key in the browser.
     */
    suspend fun getProvider()

    /**
     * Updates an existing custom provider.
     *
     * When `issuer` or `discovery_url` is changed on an OIDC provider, the server re-fetches and
     * validates the discovery document before persisting. This may return a validation error
     * (`error_code: "validation_failed"`) if the discovery document is unreachable, invalid, or
     * the issuer does not match.
     *
     * This function should only be called on a server. Never expose your `service_role` key in the browser.
     */
    suspend fun updateProvider()

    /**
     * Deletes a custom provider.
     *
     * This function should only be called on a server. Never expose your `service_role` key in the browser.
     */
    suspend fun deleteProvider(identifier: String)

}
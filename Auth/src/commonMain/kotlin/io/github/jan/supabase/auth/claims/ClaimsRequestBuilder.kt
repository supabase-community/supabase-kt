package io.github.jan.supabase.auth.claims

import io.github.jan.supabase.auth.Auth

/**
 * A builder for [Auth.getClaims]
 */
data class ClaimsRequestBuilder(
    /**
     * Whether allow expired JWTs
     */
    var allowExpired: Boolean = false,
    /**
     * A custom list of [JWK]s.
     */
    val jwks: MutableList<JWK> = mutableListOf()
)


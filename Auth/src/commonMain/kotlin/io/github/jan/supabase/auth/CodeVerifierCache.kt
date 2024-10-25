package io.github.jan.supabase.auth

import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic

/**
 * A cache for the code verifier used in the PKCE flow.
 */
interface CodeVerifierCache {

    /**
     * Saves the given code verifier.
     */
    suspend fun saveCodeVerifier(codeVerifier: String)

    /**
     * Loads the saved code verifier from the cache.
     */
    suspend fun loadCodeVerifier(): String?

    /**
     * Deletes the saved code verifier from the cache.
     */
    suspend fun deleteCodeVerifier()

}

/**
 * A [CodeVerifierCache] that uses the [AtomicRef] API.
 */
class MemoryCodeVerifierCache(codeVerifier: String? = null): CodeVerifierCache {

    private var codeVerifier by atomic(codeVerifier)

    override suspend fun saveCodeVerifier(codeVerifier: String) {
        this.codeVerifier = codeVerifier
    }

    override suspend fun loadCodeVerifier(): String? {
        return codeVerifier
    }

    override suspend fun deleteCodeVerifier() {
        codeVerifier = null
    }

}

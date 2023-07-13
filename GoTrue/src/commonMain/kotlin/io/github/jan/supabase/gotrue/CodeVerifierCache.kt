package io.github.jan.supabase.gotrue

import io.github.jan.supabase.collections.AtomicMutableMap
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
 * A [CodeVerifierCache] that uses the [AtomicMutableMap] API.
 */
class MemoryCodeVerifierCache: CodeVerifierCache {

    private var codeVerifier by atomic<String?>(null)

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

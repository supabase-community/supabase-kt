package io.github.jan.supabase.gotrue

import io.github.reactivecircus.cache4k.Cache

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
 * A [CodeVerifierCache] that uses the [Cache] API.
 */
class MemoryCodeVerifierCache(private val cache: Cache<String, String> = Cache.Builder<String, String>().build()): CodeVerifierCache {

    override suspend fun saveCodeVerifier(codeVerifier: String) {
        cache.put(SETTINGS_KEY, codeVerifier)
    }

    override suspend fun loadCodeVerifier(): String? {
        return cache.get(SETTINGS_KEY)
    }

    override suspend fun deleteCodeVerifier() {
        cache.invalidate(SETTINGS_KEY)
    }

    companion object {

        /**
         * The key used to store the code verifier in the [Settings].
         */
        const val SETTINGS_KEY = "supabase_code_verifier"

    }

}

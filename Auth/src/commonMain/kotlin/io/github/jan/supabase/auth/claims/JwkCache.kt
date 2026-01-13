package io.github.jan.supabase.auth.claims

import kotlin.concurrent.atomics.AtomicReference
import kotlin.time.Instant

data class JwkCacheEntry(
    val jwks: List<JWK>,
    val cachedAt: Instant
)

interface JwkCache {

    suspend fun get(): JwkCacheEntry?

    suspend fun set(entry: JwkCacheEntry)

}

class JwkCacheImpl: JwkCache {

    private val entry: AtomicReference<JwkCacheEntry?> = AtomicReference(null)

    override suspend fun get(): JwkCacheEntry? = entry.load()

    override suspend fun set(entry: JwkCacheEntry) {
        this.entry.store(entry)
    }

}
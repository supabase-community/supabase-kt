package io.github.jan.supabase.storage.resumable

import io.github.reactivecircus.cache4k.Cache
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * A [ResumableCache] implementation using [com.russhwolf.cache4k.Cache]. This implementation saves the urls in memory. If you want a disk based cache, use [Disk].
 * By default, cached urls expire after 30 minutes. You can change this by passing a custom [Cache] to the constructor
 */
class MemoryResumableCache(
    private val cache: Cache<String, String> = Cache.Builder<String, String>()
        .build()
) : ResumableCache {

    override suspend fun set(fingerprint: Fingerprint, entry: ResumableCacheEntry) {
        cache.put(fingerprint.value, Json.encodeToString(entry))
    }

    override suspend fun get(fingerprint: Fingerprint): ResumableCacheEntry? {
        return cache.get(fingerprint.value)?.let {
            Json.decodeFromString(it)
        }
    }

    override suspend fun remove(fingerprint: Fingerprint) {
        cache.invalidate(fingerprint.value)
    }

    override suspend fun clear() {
        cache.invalidateAll()
    }

    override suspend fun entries(): List<CachePair> {
        return cache.asMap().mapNotNull {
            Fingerprint(it.key.toString())
        }.map {
            it to Json.decodeFromString(it.value)
        }
    }

}
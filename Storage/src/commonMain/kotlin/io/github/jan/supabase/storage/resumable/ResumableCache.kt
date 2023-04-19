@file:OptIn(ExperimentalSettingsApi::class)

package io.github.jan.supabase.storage.resumable

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.Settings
import com.russhwolf.settings.coroutines.toSuspendSettings
import io.github.reactivecircus.cache4k.Cache
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class ResumableCacheEntry(val url: String, val path: String, val bucketId: String, val expiresAt: Instant)

typealias CachePair = Pair<Fingerprint, ResumableCacheEntry>

/**
 * A cache for storing resumable upload urls
 */
interface ResumableCache {

    /**
     * Stores the [entry] for the [fingerprint]. The fingerprint consists of the bucket id, the file path and the file size
     */
    suspend fun set(fingerprint: Fingerprint, entry: ResumableCacheEntry)

    /**
     * Returns the resumable cache entry for the [fingerprint] or null if no url was stored
     */
    suspend fun get(fingerprint: Fingerprint): ResumableCacheEntry?

    /**
     * Removes the entry for the [fingerprint]
     */
    suspend fun remove(fingerprint: Fingerprint)

    /**
     * Clears the cache
     */
    suspend fun clear()

    suspend fun entries(): List<CachePair>

    /**
     * A [ResumableCache] implementation using [com.russhwolf.settings.Settings]. This implementation saves the urls on the disk. If you want a memory only cache, use [Memory]
     */
    class Disk(settings: com.russhwolf.settings.Settings = Settings()) : ResumableCache {

        @OptIn(ExperimentalSettingsApi::class)
        private val settings = settings.toSuspendSettings()

        @OptIn(ExperimentalSettingsApi::class)
        override suspend fun set(fingerprint: Fingerprint, entry: ResumableCacheEntry) {
            settings.putString(fingerprint.value, Json.encodeToString(entry))
        }

        @OptIn(ExperimentalSettingsApi::class)
        override suspend fun get(fingerprint: Fingerprint): ResumableCacheEntry? {
            return settings.getStringOrNull(fingerprint.value)?.let {
                Json.decodeFromString(it)
            }
        }

        @OptIn(ExperimentalSettingsApi::class)
        override suspend fun remove(fingerprint: Fingerprint) {
            settings.remove(fingerprint.value)
        }

        @OptIn(ExperimentalSettingsApi::class)
        override suspend fun clear() {
            settings.keys().forEach {
                if(it.split("::").size == 4) remove(Fingerprint(it) ?: error("Invalid fingerprint $it"))
            }
        }

        override suspend fun entries(): List<CachePair> {
            return settings.keys().mapNotNull { key ->
                Fingerprint(key) //filter out invalid fingerprints
            }.map {
                it to (get(it) ?: error("No entry found for $it"))
            }
        }

    }

    /**
     * A [ResumableCache] implementation using [com.russhwolf.cache4k.Cache]. This implementation saves the urls in memory. If you want a disk based cache, use [Disk].
     * By default, cached urls expire after 30 minutes. You can change this by passing a custom [Cache] to the constructor
     */
    class Memory(
        private val cache: Cache<String, String> = Cache.Builder()
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

}
@file:OptIn(ExperimentalSettingsApi::class)

package io.github.jan.supabase.storage.resumable

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.Settings
import com.russhwolf.settings.coroutines.toSuspendSettings
import io.github.reactivecircus.cache4k.Cache
import kotlin.time.Duration.Companion.minutes

/**
 * A cache for storing resumable upload urls
 */
interface ResumableCache {

    /**
     * Stores the [url] for the [fingerprint]. The fingerprint consists of the bucket id, the file path and the file size
     */
    suspend fun set(fingerprint: Fingerprint, url: String)

    /**
     * Returns the url for the [fingerprint] or null if no url was stored
     */
    suspend fun get(fingerprint: Fingerprint): String?

    /**
     * Removes the url for the [fingerprint]
     */
    suspend fun remove(fingerprint: Fingerprint)

    /**
     * Clears the cache
     */
    suspend fun clear()

    /**
     * A [ResumableCache] implementation using [com.russhwolf.settings.Settings]. This implementation saves the urls on the disk. If you want a memory only cache, use [Memory]
     */
    class Disk(settings: com.russhwolf.settings.Settings = Settings()) : ResumableCache {

        @OptIn(ExperimentalSettingsApi::class)
        private val settings = settings.toSuspendSettings()

        @OptIn(ExperimentalSettingsApi::class)
        override suspend fun set(fingerprint: Fingerprint, url: String) {
            settings.putString(fingerprint.value, url)
        }

        @OptIn(ExperimentalSettingsApi::class)
        override suspend fun get(fingerprint: Fingerprint): String? {
            settings.clear()
            return settings.getStringOrNull(fingerprint.value)
        }

        override suspend fun remove(fingerprint: Fingerprint) {
            settings.remove(fingerprint.value)
        }

        @OptIn(ExperimentalSettingsApi::class)
        override suspend fun clear() {
            settings.clear()
        }

    }

    /**
     * A [ResumableCache] implementation using [com.russhwolf.cache4k.Cache]. This implementation saves the urls in memory. If you want a disk based cache, use [Disk].
     * By default, cached urls expire after 30 minutes. You can change this by passing a custom [Cache] to the constructor
     */
    class Memory(
        private val cache: Cache<String, String> = Cache.Builder()
            .expireAfterWrite(30.minutes)
            .build()
    ) : ResumableCache {

        override suspend fun set(fingerprint: Fingerprint, url: String) {
            cache.put(fingerprint.value, url)
        }

        override suspend fun get(fingerprint: Fingerprint): String? {
            return cache.get(fingerprint.value)
        }

        override suspend fun remove(fingerprint: Fingerprint) {
            cache.invalidate(fingerprint.value)
        }

        override suspend fun clear() {
            cache.invalidateAll()
        }

    }

}
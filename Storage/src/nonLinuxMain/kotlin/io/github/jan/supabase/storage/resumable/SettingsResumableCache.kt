package io.github.jan.supabase.storage.resumable

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.Settings
import com.russhwolf.settings.coroutines.toSuspendSettings
import io.github.jan.supabase.annotations.SupabaseInternal
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * A [ResumableCache] implementation using [com.russhwolf.settings.Settings]. This implementation saves the urls on the disk. If you want a memory only cache, use [Memory].
 * Unsupported on Linux.
 */
class SettingsResumableCache(settings: Settings = Settings()) : ResumableCache {

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
            if(it.split(Fingerprint.FINGERPRINT_SEPARATOR).size == Fingerprint.FINGERPRINT_PARTS) remove(Fingerprint(it) ?: error("Invalid fingerprint $it"))
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

@SupabaseInternal
actual fun createDefaultResumableCache(): ResumableCache = SettingsResumableCache()
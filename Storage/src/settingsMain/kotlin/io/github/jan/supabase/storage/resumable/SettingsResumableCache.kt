package io.github.jan.supabase.storage.resumable

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.Settings
import io.github.jan.supabase.annotations.SupabaseInternal
import io.ktor.util.PlatformUtils.IS_NODE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * A [ResumableCache] implementation using [com.russhwolf.settings.Settings]. This implementation saves the urls on the disk. If you want a memory only cache, use [Memory].
 * Unsupported on Linux.
 */
class SettingsResumableCache(private val settings: Settings = Settings()) : ResumableCache {

    //@OptIn(ExperimentalSettingsApi::class)
    //private val settings = settings.toSuspendSettings()

    @OptIn(ExperimentalSettingsApi::class)
    override suspend fun set(fingerprint: Fingerprint, entry: ResumableCacheEntry) {
        withContext(Dispatchers.Default) {
            settings.putString(fingerprint.value, Json.encodeToString(entry))
        }
    }

    @OptIn(ExperimentalSettingsApi::class)
    override suspend fun get(fingerprint: Fingerprint): ResumableCacheEntry? {
        return withContext(Dispatchers.Default) {
            settings.getStringOrNull(fingerprint.value)?.let {
                Json.decodeFromString(it)
            }
        }
    }

    @OptIn(ExperimentalSettingsApi::class)
    override suspend fun remove(fingerprint: Fingerprint) {
        withContext(Dispatchers.Default) {
            settings.remove(fingerprint.value)
        }
    }

    @OptIn(ExperimentalSettingsApi::class)
    override suspend fun clear() {
        withContext(Dispatchers.Default) {
            settings.keys.forEach {
                if(it.split(Fingerprint.FINGERPRINT_SEPARATOR).size == Fingerprint.FINGERPRINT_PARTS) remove(Fingerprint(it) ?: error("Invalid fingerprint $it"))
            }
        }
    }

    override suspend fun entries(): List<CachePair> {
        return withContext(Dispatchers.Default) {
            settings.keys.mapNotNull { key ->
                Fingerprint(key) //filter out invalid fingerprints
            }.map {
                it to (get(it) ?: error("No entry found for $it"))
            }
        }
    }

}

@SupabaseInternal
actual fun createDefaultResumableCache(): ResumableCache = if(!IS_NODE) SettingsResumableCache() else MemoryResumableCache()
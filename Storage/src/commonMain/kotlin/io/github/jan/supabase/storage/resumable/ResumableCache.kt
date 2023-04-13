@file:OptIn(ExperimentalSettingsApi::class)

package io.github.jan.supabase.storage.resumable

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.coroutines.toSuspendSettings

interface ResumableCache {

    suspend fun set(fingerprint: String, url: String)

    suspend fun get(fingerprint: String): String?

    class Settings(settings: com.russhwolf.settings.Settings) : ResumableCache {

        private val settings = settings.toSuspendSettings()

        @OptIn(ExperimentalSettingsApi::class)
        override suspend fun set(fingerprint: String, url: String) {
            settings.putString(fingerprint, url)
        }

        @OptIn(ExperimentalSettingsApi::class)
        override suspend fun get(fingerprint: String): String? {
            return settings.getStringOrNull(fingerprint)
        }

    }

}
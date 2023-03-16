@file:OptIn(ExperimentalSettingsApi::class)
package io.github.jan.supabase.gotrue

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.Settings
import com.russhwolf.settings.coroutines.toSuspendSettings
import io.github.aakira.napier.Napier
import io.github.jan.supabase.gotrue.user.UserSession
import io.github.jan.supabase.supabaseJson
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

interface SessionManager {

    suspend fun saveSession(session: UserSession)

    suspend fun loadSession(): UserSession?

    suspend fun deleteSession()

}

/**
 * Represents the session manager. Used for saving and restoring the session from storage
 */
class SettingsSessionManager(settings: Settings = createDefaultSettings()): SessionManager {

    private val suspendSettings = settings.toSuspendSettings()

    @OptIn(ExperimentalSettingsApi::class)
    override suspend fun saveSession(session: UserSession) {
        suspendSettings.putString(SETTINGS_KEY, supabaseJson.encodeToString(session))
    }

    @OptIn(ExperimentalSettingsApi::class)
    override suspend fun loadSession(): UserSession? {
        val session = suspendSettings.getStringOrNull(SETTINGS_KEY) ?: return null
        return try {
            supabaseJson.decodeFromString(session)
        } catch(e: Exception) {
            Napier.e(e) { "Failed to load session" }
            null
        }
    }

    @OptIn(ExperimentalSettingsApi::class)
    override suspend fun deleteSession() {
        suspendSettings.remove(SETTINGS_KEY)
    }

    companion object {

        const val SETTINGS_KEY = "session"

    }

}

fun createDefaultSettings() = try {
    Settings()
} catch(e: Exception) {
    throw IllegalStateException("Failed to create default settings for SettingsSessionManager. You might have to provide a custom settings instance or a custom session manager. Learn more at https://github.com/supabase-community/supabase-kt/wiki/Session-Saving")
}
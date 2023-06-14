@file:OptIn(ExperimentalSettingsApi::class)
package io.github.jan.supabase.gotrue

import co.touchlab.kermit.Logger
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.Settings
import com.russhwolf.settings.coroutines.toSuspendSettings
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.gotrue.user.UserSession
import io.github.jan.supabase.supabaseJson
import kotlinx.serialization.encodeToString

/**
 * Represents the session manager. Used for saving and restoring the session from storage
 */
interface SessionManager {

    /**
     * Saves the given session.
     */
    suspend fun saveSession(session: UserSession)

    /**
     * Loads the saved session from storage.
     */
    suspend fun loadSession(): UserSession?

    /**
     * Deletes the saved session from storage.
     */
    suspend fun deleteSession()

}

/**
 * A [SessionManager] that uses the [Settings] API.
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
            Logger.e(e) { "Failed to load session" }
            null
        }
    }

    @OptIn(ExperimentalSettingsApi::class)
    override suspend fun deleteSession() {
        suspendSettings.remove(SETTINGS_KEY)
    }

    companion object {

        /**
         * The key used for saving the session
         */
        const val SETTINGS_KEY = "session"

    }

}

@SupabaseInternal
fun createDefaultSettings() = try {
    Settings()
} catch(e: Exception) {
    error("Failed to create default settings for SettingsSessionManager. You might have to provide a custom settings instance or a custom session manager. Learn more at https://github.com/supabase-community/supabase-kt/wiki/Session-Saving")
}
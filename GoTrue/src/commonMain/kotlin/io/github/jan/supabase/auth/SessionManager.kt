@file:OptIn(ExperimentalSettingsApi::class)
package io.github.jan.supabase.auth

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.coroutines.SuspendSettings
import io.github.aakira.napier.Napier
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.supabaseJson
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

/**
 * Represents the session manager. Used for saving and restoring the session from storage
 */
class SessionManager(private val settings: SuspendSettings) {

    @OptIn(ExperimentalSettingsApi::class)
    suspend fun saveSession(session: UserSession) {
        settings.putString("session", supabaseJson.encodeToString(session))
    }

    @OptIn(ExperimentalSettingsApi::class)
    suspend fun loadSession(): UserSession? {
        val session = settings.getStringOrNull("session") ?: return null
        return try {
            supabaseJson.decodeFromString(session)
        } catch(e: Exception) {
            Napier.e(e) { "Failed to load session" }
            null
        }
    }

    @OptIn(ExperimentalSettingsApi::class)
    suspend fun deleteSession() {
        settings.remove("session")
    }

}
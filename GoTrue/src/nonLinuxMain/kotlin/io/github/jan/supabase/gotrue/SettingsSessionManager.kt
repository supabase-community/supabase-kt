package io.github.jan.supabase.gotrue

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.Settings
import com.russhwolf.settings.coroutines.toSuspendSettings
import io.github.jan.supabase.gotrue.user.UserSession
import io.github.jan.supabase.logging.SupabaseLogger
import io.github.jan.supabase.logging.e
import io.github.jan.supabase.supabaseJson
import kotlinx.serialization.encodeToString

/**
 * A [SessionManager] that uses the [Settings] API.
 */
class SettingsSessionManager(
    settings: Settings = createDefaultSettings(),
    private val logger: SupabaseLogger //Some better solution as this doesn't really work
) : SessionManager {

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
            logger.e(e) { "Failed to load session" }
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
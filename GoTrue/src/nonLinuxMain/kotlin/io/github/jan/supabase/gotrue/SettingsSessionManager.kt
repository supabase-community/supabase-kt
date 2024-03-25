package io.github.jan.supabase.gotrue

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.Settings
import io.github.jan.supabase.gotrue.user.UserSession
import io.github.jan.supabase.logging.e
import io.github.jan.supabase.supabaseJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString

/**
 * A [SessionManager] that uses the [Settings] API.
 */
class SettingsSessionManager(
    private val settings: Settings = createDefaultSettings()
) : SessionManager {

    //private val suspendSettings = settings.toSuspendSettings()

    @OptIn(ExperimentalSettingsApi::class)
    override suspend fun saveSession(session: UserSession) {
        withContext(Dispatchers.Default) {
            settings.putString(SETTINGS_KEY, supabaseJson.encodeToString(session))
        }
    }

    @OptIn(ExperimentalSettingsApi::class)
    override suspend fun loadSession(): UserSession? {
        return withContext(Dispatchers.Default) {
            val session = settings.getStringOrNull(SETTINGS_KEY) ?: return@withContext null
            try {
                supabaseJson.decodeFromString(session)
            } catch(e: Exception) {
                Auth.logger.e(e) { "Failed to load session" }
                null
            }
        }
    }

    @OptIn(ExperimentalSettingsApi::class)
    override suspend fun deleteSession() {
        withContext(Dispatchers.Default) {
            settings.remove(SETTINGS_KEY)
        }
    }

    companion object {

        /**
         * The key used for saving the session
         */
        const val SETTINGS_KEY = "session"

    }

}
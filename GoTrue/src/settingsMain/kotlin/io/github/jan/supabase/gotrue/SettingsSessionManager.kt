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
 *
 * @param settings The [Settings] instance to use. Defaults to [createDefaultSettings].
 * @param key The key to use for saving the session.
 */
class SettingsSessionManager(
    private val settings: Settings = createDefaultSettings(),
    private val key: String = SETTINGS_KEY,
) : SessionManager {

    init {
        checkForOldSession()
    }

    private fun checkForOldSession() {
        if (key == SETTINGS_KEY) return

        val oldSession = settings.getStringOrNull(SETTINGS_KEY)
        val newSession = settings.getStringOrNull(key)

        if (oldSession != null && newSession == null) {
            settings.putString(key, oldSession)
            settings.remove(SETTINGS_KEY)
        }
    }

    @OptIn(ExperimentalSettingsApi::class)
    override suspend fun saveSession(session: UserSession) {
        withContext(Dispatchers.Default) {
            settings.putString(key, supabaseJson.encodeToString(session))
        }
    }

    @OptIn(ExperimentalSettingsApi::class)
    override suspend fun loadSession(): UserSession? {
        val session = withContext(Dispatchers.Default) {
            settings.getStringOrNull(key)
        } ?: return null
        return try {
            supabaseJson.decodeFromString(session)
        } catch(e: Exception) {
            Auth.logger.e(e) { "Failed to load session" }
            null
        }
    }

    @OptIn(ExperimentalSettingsApi::class)
    override suspend fun deleteSession() {
        withContext(Dispatchers.Default) {
            settings.remove(key)
        }
    }

    companion object {

        /**
         * The key used for saving the session
         */
        const val SETTINGS_KEY = "session"

    }

}
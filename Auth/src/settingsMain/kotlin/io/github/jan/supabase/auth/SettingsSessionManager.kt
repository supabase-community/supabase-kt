package io.github.jan.supabase.auth

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.Settings
import com.russhwolf.settings.coroutines.toSuspendSettings
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.logging.e
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonBuilder

private val settingsJson = Json {
    encodeDefaults = true
}

/**
 * A [SessionManager] that uses the [Settings] API.
 *
 * @param settings The [Settings] instance to use. Defaults to [createDefaultSettings].
 * @param key The key to use for saving the session.
 * @param json The [Json] instance to use for serialization. Defaults to [settingsJson]. **Important: [JsonBuilder.encodeDefaults] must be set to true.**
 */
@OptIn(ExperimentalSettingsApi::class)
class SettingsSessionManager(
    private val settings: Settings = createDefaultSettings(),
    private val key: String = SETTINGS_KEY,
    private val json: Json = settingsJson
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

    private val suspendSettings = settings.toSuspendSettings()

    override suspend fun saveSession(session: UserSession) {
        suspendSettings.putString(key, json.encodeToString(session))
    }

    override suspend fun loadSession(): UserSession? {
        val session = suspendSettings.getStringOrNull(key) ?: return null
        return try {
            json.decodeFromString(session)
        } catch(e: Exception) {
            currentCoroutineContext().ensureActive()
            Auth.logger.e(e) { "Failed to load session" }
            null
        }
    }

    override suspend fun deleteSession() {
        suspendSettings.remove(key)
    }

    companion object {

        /**
         * The key used for saving the session
         */
        const val SETTINGS_KEY = "session"

    }

}
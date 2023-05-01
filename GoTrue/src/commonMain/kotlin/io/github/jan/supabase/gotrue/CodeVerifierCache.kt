package io.github.jan.supabase.gotrue

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.Settings
import com.russhwolf.settings.coroutines.toSuspendSettings

/**
 * A cache for the code verifier used in the PKCE flow.
 */
interface CodeVerifierCache {

    /**
     * Saves the given code verifier.
     */
    suspend fun saveCodeVerifier(codeVerifier: String)

    /**
     * Loads the saved code verifier from the cache.
     */
    suspend fun loadCodeVerifier(): String?

    /**
     * Deletes the saved code verifier from the cache.
     */
    suspend fun deleteCodeVerifier()

}

/**
 * A [CodeVerifierCache] that uses the [Settings] API.
 */
class SettingsCodeVerifierCache(settings: Settings = createDefaultSettings()): CodeVerifierCache {

    @OptIn(ExperimentalSettingsApi::class)
    private val suspendSettings = settings.toSuspendSettings()

    @OptIn(ExperimentalSettingsApi::class, ExperimentalSettingsApi::class)
    override suspend fun saveCodeVerifier(codeVerifier: String) {
        suspendSettings.putString(SETTINGS_KEY, codeVerifier)
    }

    @OptIn(ExperimentalSettingsApi::class)
    override suspend fun loadCodeVerifier(): String? {
        return suspendSettings.getStringOrNull(SETTINGS_KEY)
    }

    @OptIn(ExperimentalSettingsApi::class)
    override suspend fun deleteCodeVerifier() {
        suspendSettings.remove(SETTINGS_KEY)
    }

    companion object {

        const val SETTINGS_KEY = "supabase_code_verifier"

    }

}
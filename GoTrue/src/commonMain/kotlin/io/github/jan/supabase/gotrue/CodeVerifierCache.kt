package io.github.jan.supabase.gotrue

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.Settings
import com.russhwolf.settings.coroutines.toSuspendSettings

interface CodeVerifierCache {

    suspend fun saveCodeVerifier(codeVerifier: String)

    suspend fun loadCodeVerifier(): String?

    suspend fun deleteCodeVerifier()

}

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
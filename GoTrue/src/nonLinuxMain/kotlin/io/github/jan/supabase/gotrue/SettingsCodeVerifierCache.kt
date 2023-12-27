package io.github.jan.supabase.gotrue

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * A [CodeVerifierCache] that uses the [Settings] API.
 */
class SettingsCodeVerifierCache(private val settings: Settings = createDefaultSettings()): CodeVerifierCache {

    //@OptIn(ExperimentalSettingsApi::class)
    //private val suspendSettings = settings.toSuspendSettings()

    @OptIn(ExperimentalSettingsApi::class, ExperimentalSettingsApi::class)
    override suspend fun saveCodeVerifier(codeVerifier: String) {
        withContext(Dispatchers.Default) {
            settings.putString(SETTINGS_KEY, codeVerifier)
        }
    }

    @OptIn(ExperimentalSettingsApi::class)
    override suspend fun loadCodeVerifier(): String? {
        return withContext(Dispatchers.Default) {
            settings.getStringOrNull(SETTINGS_KEY)
        }
    }

    @OptIn(ExperimentalSettingsApi::class)
    override suspend fun deleteCodeVerifier() {
        withContext(Dispatchers.Default) {
            settings.remove(SETTINGS_KEY)
        }
    }

    companion object {

        /**
         * The key used to store the code verifier in the [Settings].
         */
        const val SETTINGS_KEY = "supabase_code_verifier"

    }

}

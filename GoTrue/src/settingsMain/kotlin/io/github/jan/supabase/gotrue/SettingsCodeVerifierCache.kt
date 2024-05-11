package io.github.jan.supabase.gotrue

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * A [CodeVerifierCache] that uses the [Settings] API.
 *
 * @param settings The [Settings] instance to use. Defaults to [createDefaultSettings].
 * @param key The key to use for saving the code verifier.
 */
class SettingsCodeVerifierCache(
    private val settings: Settings = createDefaultSettings(),
    private val key: String = SETTINGS_KEY,
): CodeVerifierCache {

    init {
        checkForOldCodeVerifier()
    }

    private fun checkForOldCodeVerifier() {
        if (key == SETTINGS_KEY) return

        val oldSession = settings.getStringOrNull(SETTINGS_KEY)
        val newSession = settings.getStringOrNull(key)

        if (oldSession != null && newSession == null) {
            settings.putString(key, oldSession)
            settings.remove(SETTINGS_KEY)
        }
    }


    @OptIn(ExperimentalSettingsApi::class, ExperimentalSettingsApi::class)
    override suspend fun saveCodeVerifier(codeVerifier: String) {
        withContext(Dispatchers.Default) {
            settings.putString(key, codeVerifier)
        }
    }

    @OptIn(ExperimentalSettingsApi::class)
    override suspend fun loadCodeVerifier(): String? {
        return withContext(Dispatchers.Default) {
            settings.getStringOrNull(key)
        }
    }

    @OptIn(ExperimentalSettingsApi::class)
    override suspend fun deleteCodeVerifier() {
        withContext(Dispatchers.Default) {
            settings.remove(key)
        }
    }

    companion object {

        /**
         * The key used to store the code verifier in the [Settings].
         */
        const val SETTINGS_KEY = "supabase_code_verifier"

    }

}

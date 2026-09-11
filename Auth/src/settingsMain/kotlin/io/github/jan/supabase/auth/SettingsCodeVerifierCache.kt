package io.github.jan.supabase.auth

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.Settings
import com.russhwolf.settings.coroutines.toSuspendSettings

/**
 * A [CodeVerifierCache] that uses the [Settings] API.
 *
 * @param settings The [Settings] instance to use. Defaults to [createDefaultSettings].
 * @param key The key to use for saving the code verifier.
 */
@OptIn(ExperimentalSettingsApi::class)
class SettingsCodeVerifierCache(
    private val settings: Settings = createDefaultSettings(),
    private val storageKey: String = SETTINGS_KEY,
): CodeVerifierCache {

    private val suspendSettings = settings.toSuspendSettings()

    suspend fun getPKCEFlowIndex() {
        suspendSettings.getString()
    }

    override suspend fun storePKCEVerifier(flowId: String, verifier: String, onEvictFlow: (String) -> Unit = {}) {
        val key = pkceVerifierSlotKey(storageKey, flowId)
        suspendSettings.putString(key, verifier)
        val index = suspendSettings
        suspendSettings.putString(pkceLegacyKey(storageKey), verifier) // for legacy compatibility
    }

    suspend fun retrievePKCEVerifier(flowId: String?): String? {
        return if(flowId != null) {
            suspendSettings.getString(pkceVerifierSlotKey(storageKey, flowId))
        } else {
            suspendSettings.getString(pkceLegacyKey(storageKey))
        }
    }

    suspend fun removePKCEVerifier(flowId: String?) {
        if(flowId == null) {
            return suspendSettings.remove(pkceLegacyKey(storageKey))
        }
        val slotKey = pkceVerifierSlotKey(storageKey, flowId)
        val slotValue = suspendSettings.getString(slotKey)
        suspendSettings.remove(slotKey)
        suspendSettings.remove(flowId)

        val legacyKey = pkceLegacyKey(storageKey)
        if(slotValue == suspendSettings.getString(legacyKey)) {
            suspendSettings.remove(legacyKey)
        }
    }

    suspend fun removeAllPKCEVerifiers() {
        clearFlows()
        removeValue(pkceLegacyKey(storageKey))
    }

    companion object {

        /**
         * The key used to store the code verifier in the [Settings].
         */
        const val SETTINGS_KEY = "supabase_code_verifier"

    }

}

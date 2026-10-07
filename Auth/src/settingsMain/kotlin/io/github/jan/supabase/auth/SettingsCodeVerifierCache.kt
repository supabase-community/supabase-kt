package io.github.jan.supabase.auth

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.Settings
import com.russhwolf.settings.coroutines.toSuspendSettings
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecodingException

/**
 * A [CodeVerifierCache] that uses the [Settings] API.
 *
 * @param settings The [Settings] instance to use. Defaults to [createDefaultSettings].
 * @param storageKey The key to use for saving the code verifier.
 */
@OptIn(ExperimentalSettingsApi::class)
class SettingsCodeVerifierCache(
    private val settings: Settings = createDefaultSettings(),
    val storageKey: String = SETTINGS_KEY,
): CodeVerifierCache {

    private val suspendSettings = settings.toSuspendSettings()

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun getPKCEFlowIndex(): List<String> {
        val value = suspendSettings.getString(pkceFlowIndexKey(storageKey), "[]")
        return try {
            Json.decodeFromString(value)
        } catch(e: JsonDecodingException) {
            // possibly log ?
            emptyList()
        }
    }

    suspend fun writePKCEFlowIndex(newValue: List<String>) {
        suspendSettings.putString(pkceFlowIndexKey(storageKey), Json.encodeToString(newValue))
    }

    override suspend fun storePKCEVerifier(flowId: String, verifier: String, onEvictFlow: (String) -> Unit) {
        val key = pkceVerifierSlotKey(storageKey, flowId)
        suspendSettings.putString(key, verifier)
        val index = getPKCEFlowIndex().toMutableList()
        // Add the new flow to the index
        if (!index.contains(flowId)) {
            index.add(flowId)
        }
        // Evict oldest flows if we exceed max concurrent flows
        while(index.size > PKCEConstants.PKCE_MAX_CONCURRENT_FLOWS) {
            val evicted = index.removeFirst()
            suspendSettings.remove(pkceVerifierSlotKey(storageKey, evicted))
            onEvictFlow(evicted)
        }
        writePKCEFlowIndex(index)
        suspendSettings.putString(pkceLegacyKey(storageKey), verifier) // for legacy compatibility
    }

    override suspend fun retrievePKCEVerifier(flowId: String?): String? {
        return if(flowId != null) {
            suspendSettings.getStringOrNull(pkceVerifierSlotKey(storageKey, flowId))
        } else {
            suspendSettings.getStringOrNull(pkceLegacyKey(storageKey))
        }
    }

    override suspend fun removePKCEVerifier(flowId: String?) {
        if(flowId == null) {
            return suspendSettings.remove(pkceLegacyKey(storageKey))
        }
        val slotKey = pkceVerifierSlotKey(storageKey, flowId)
        val slotValue = suspendSettings.getStringOrNull(slotKey)
        suspendSettings.remove(slotKey)

        val index = getPKCEFlowIndex()
        val remaining = index.filter { it != flowId }
        if(remaining.size != index.size) {
            if(remaining.isNotEmpty()) {
                writePKCEFlowIndex(remaining)
            } else {
                suspendSettings.remove(pkceFlowIndexKey(storageKey))
            }
        }

        val legacyKey = pkceLegacyKey(storageKey)
        if(slotValue != null && slotValue == suspendSettings.getStringOrNull(legacyKey)) {
            suspendSettings.remove(legacyKey)
        }
    }

    override suspend fun removeAllPKCEVerifiers() {
        val index = getPKCEFlowIndex()
        for (flowId in index) {
            suspendSettings.remove(pkceVerifierSlotKey(storageKey, flowId))
        }
        suspendSettings.remove(pkceFlowIndexKey(storageKey))
        suspendSettings.remove(pkceLegacyKey(storageKey))
    }

    companion object {

        /**
         * The key used to store the code verifier in the [Settings].
         */
        const val SETTINGS_KEY = "supabase_code_verifier"

    }

}

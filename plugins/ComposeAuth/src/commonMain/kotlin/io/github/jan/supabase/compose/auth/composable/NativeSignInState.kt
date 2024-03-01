package io.github.jan.supabase.compose.auth.composable

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.jan.supabase.SupabaseSerializer
import io.github.jan.supabase.encodeToJsonElement
import io.ktor.util.generateNonce
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

/**
 * Represents the state of a Native Auth flow
 */
class NativeSignInState(
    @PublishedApi internal val serializer: SupabaseSerializer
) {

    @PublishedApi internal var _status: NativeSignInStatus by mutableStateOf(NativeSignInStatus.NotStarted)

    /**
     * The current status of the flow
     */
    val status: NativeSignInStatus
        get() = _status

    /**
     * Starts the Native Auth flow (or the fallback, if not supported)
     * @param nonce The nonce to use for the flow (doesn't apply for the fallback)
     * @param extraData Extra data to pass to the flow
     */
    fun startFlow(
        nonce: String? = generateNonce(),
        extraData: JsonObject? = null
    ) {
        _status = NativeSignInStatus.Started(nonce, extraData)
    }

    /**
     * Starts the Native Auth flow (or the fallback, if not supported)
     * @param nonce The nonce to use for the flow (doesn't apply for the fallback)
     * @param extraData Extra data to pass to the flow
     */
    inline fun <reified T : Any> startFlow(
        extraData: T,
        nonce: String? = generateNonce(),
    ) {
        _status = NativeSignInStatus.Started(nonce, serializer.encodeToJsonElement(extraData).jsonObject)
    }

    internal fun reset() {
        _status = NativeSignInStatus.NotStarted
    }

}

/**
 * Represents the status of a Native Auth flow
 */
sealed interface NativeSignInStatus {

    /**
     * The flow has started
     * @param nonce The nonce to use for the flow
     * @param extraData Extra data to pass into the sign in
     */
    data class Started(val nonce: String?, val extraData: JsonObject?) : NativeSignInStatus

    /**
     * The flow has not started
     */
    data object NotStarted : NativeSignInStatus
}
package io.github.jan.supabase.compose.auth.composable

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Represents the state of a Native Auth flow
 */
class NativeSignInState {

    /**
     * Whether the flow has started
     */
    var started by mutableStateOf(false)
        private set

    /**
     * Starts the Native Auth flow (or the fallback, if not supported)
     */
    fun startFlow() {
        started = true
    }

    internal fun reset() {
        started = false
    }
}
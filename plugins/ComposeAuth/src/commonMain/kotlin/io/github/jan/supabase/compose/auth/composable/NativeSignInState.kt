package io.github.jan.supabase.compose.auth.composable

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue


/**
 * State of Auth flow
 */
class NativeSignInState {

    /**
     * start value of Auth flow
     */
    var started by mutableStateOf(false)
        private set

    /**
     * starts SignIn flow
     */
    fun startFlow() {
        started = true
    }

    internal fun reset() {
        started = false
    }

}
package io.github.jan.supabase.compose.auth.composable

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class NativeSignInState {

    var started by mutableStateOf(false)
        private set

    fun startFlow() {
        started = true
    }

    internal fun reset() {
        started = false
    }

}
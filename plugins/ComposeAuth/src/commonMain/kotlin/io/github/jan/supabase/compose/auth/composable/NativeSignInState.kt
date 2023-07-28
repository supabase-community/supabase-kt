package io.github.jan.supabase.compose.auth.composable

import androidx.compose.runtime.*

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
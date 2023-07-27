package io.github.temk0.supabase.authui.ui

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
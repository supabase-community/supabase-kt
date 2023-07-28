package io.github.jan.supabase.compose.auth.composable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import io.github.jan.supabase.compose.auth.ComposeAuth

@Composable
expect fun ComposeAuth.rememberLoginWithGoogle(onResult: (NativeSignInResult) -> Unit = {}, fallback: suspend () -> Unit = { this.fallbackLogin() }): NativeSignInState

@Composable
fun defaultLoginBehavior(fallback: suspend () -> Unit):NativeSignInState{
    val state = NativeSignInState()
    LaunchedEffect(key1 = state.started) {
        if (state.started) {
            fallback.invoke()
            state.reset()
        }
    }
    return state
}
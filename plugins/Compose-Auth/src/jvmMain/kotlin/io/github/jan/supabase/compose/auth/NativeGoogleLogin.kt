package io.github.jan.supabase.compose.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
actual fun NativeGoogleLogin(
    state: NativeSignInState,
    clientId: String,
    nonce: String?,
    onResult: (NativeSignInResult) -> Unit,
    fallback: (() -> Unit)?
) {
    LaunchedEffect(state.started) {
        if (state.started) {
            fallback?.invoke()
            state.reset()
        }
    }
}

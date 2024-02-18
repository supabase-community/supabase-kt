package io.github.jan.supabase.compose.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.compose.auth.composable.NativeSignInState

/**
 * Composable of default behavior if Native Auth is not supported on the platform
 */
@Composable
@SupabaseInternal
fun defaultLoginBehavior(fallback: suspend () -> Unit): NativeSignInState {
    val state = remember { NativeSignInState() }
    LaunchedEffect(key1 = state.started) {
        if (state.started) {
            fallback.invoke()
            state.reset()
        }
    }
    return state
}
package io.github.jan.supabase.compose.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.compose.auth.composable.NativeSignInState
import io.github.jan.supabase.gotrue.SignOutScope

/**
 * Composable used to sign out from Auth
 */
@Composable
@SupabaseInternal
fun ComposeAuth.defaultSignOutBehavior(signOutScope: SignOutScope, nativeSignOut: suspend () -> Unit = {}): NativeSignInState {
    val state = remember { NativeSignInState() }
    LaunchedEffect(key1 = state.started) {
        if (state.started) {
            nativeSignOut.invoke()
            signOut(signOutScope)
            state.reset()
        }
    }
    return state
}

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
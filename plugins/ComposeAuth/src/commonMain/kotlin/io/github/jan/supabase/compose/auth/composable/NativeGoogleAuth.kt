package io.github.jan.supabase.compose.auth.composable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.fallbackLogin
import io.github.jan.supabase.compose.auth.signOut
import io.github.jan.supabase.gotrue.LogoutScope
import io.github.jan.supabase.gotrue.providers.Google

/**
 * Composable function what implements Native Google Login flow with default fallback
 * @return [NativeSignInState]
 */
@Composable
expect fun ComposeAuth.rememberLoginWithGoogle(onResult: (NativeSignInResult) -> Unit = {}, fallback: suspend () -> Unit = { fallbackLogin(Google) }): NativeSignInState

/**
 * Composable of default behavior if Native Login is not supported on the platform
 */
@Composable
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


/**
 * Composable for sign out flow
 * @return [NativeSignInState]
 */
@Composable
expect fun ComposeAuth.rememberSignOut(logoutScope: LogoutScope = LogoutScope.LOCAL): NativeSignInState

/**
 * Composable used to Sign Out from GoTrue
 */
@Composable
fun ComposeAuth.defaultSignOutBehavior(logoutScope: LogoutScope, nativeSignOut: suspend () -> Unit = {}): NativeSignInState {
    val state = remember { NativeSignInState() }
    LaunchedEffect(key1 = state.started) {
        if (state.started) {
            nativeSignOut.invoke()
            signOut(logoutScope)
            state.reset()
        }
    }
    return state
}
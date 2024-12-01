package io.supabase.compose.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import io.supabase.annotations.SupabaseInternal
import io.supabase.compose.auth.composable.NativeSignInState
import io.supabase.compose.auth.composable.NativeSignInStatus
import io.supabase.logging.d

/**
 * Composable of default behavior if Native Auth is not supported on the platform
 */
@Composable
@SupabaseInternal
fun ComposeAuth.defaultLoginBehavior(fallback: suspend () -> Unit): NativeSignInState {
    val state = remember { NativeSignInState(serializer) }
    LaunchedEffect(key1 = state.status) {
        if (state.status is NativeSignInStatus.Started) {
            ComposeAuth.logger.d { "Native Auth is not supported on this platform, falling back to default behavior"}
            fallback.invoke()
            state.reset()
        }
    }
    return state
}
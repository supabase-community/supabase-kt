package io.supabase.compose.auth.composable

import androidx.compose.runtime.Composable
import io.supabase.auth.providers.Google
import io.supabase.compose.auth.ComposeAuth
import io.supabase.compose.auth.fallbackLogin

/**
 * Composable function that implements Native Google Auth.
 *
 * On unsupported platforms it will use the [fallback]
 *
 * @param onResult Callback for the result of the login
 * @param fallback Fallback function for unsupported platforms
 * @return [NativeSignInState]
 */
@Composable
expect fun ComposeAuth.rememberSignInWithGoogle(
    onResult: (NativeSignInResult) -> Unit = {},
    fallback: suspend () -> Unit = {
        fallbackLogin(Google)
    }
): NativeSignInState

internal expect suspend fun handleGoogleSignOut()
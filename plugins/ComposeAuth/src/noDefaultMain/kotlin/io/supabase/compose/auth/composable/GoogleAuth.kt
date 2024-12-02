package io.supabase.compose.auth.composable

import androidx.compose.runtime.Composable
import io.supabase.compose.auth.ComposeAuth
import io.supabase.compose.auth.defaultLoginBehavior
import io.supabase.compose.auth.composable.NativeSignInResult
import io.supabase.compose.auth.composable.NativeSignInState

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
actual fun ComposeAuth.rememberSignInWithGoogle(onResult: (NativeSignInResult) -> Unit, fallback: suspend () -> Unit): NativeSignInState = defaultLoginBehavior(fallback)

internal actual suspend fun handleGoogleSignOut() = Unit
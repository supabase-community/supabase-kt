package io.supabase.compose.auth.composable

import androidx.compose.runtime.Composable
import io.supabase.compose.auth.ComposeAuth
import io.supabase.compose.auth.defaultLoginBehavior

/**
 * Composable function that implements Native Apple Auth.
 *
 * On unsupported platforms it will use the [fallback]
 *
 * @param onResult Callback for the result of the login
 * @param fallback Fallback function for unsupported platforms
 * @return [NativeSignInState]
 */
@Composable
actual fun ComposeAuth.rememberSignInWithApple(onResult: (NativeSignInResult) -> Unit, fallback: suspend () -> Unit): NativeSignInState = defaultLoginBehavior(fallback)
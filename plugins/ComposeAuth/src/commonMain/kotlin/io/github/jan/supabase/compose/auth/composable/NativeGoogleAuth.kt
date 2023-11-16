package io.github.jan.supabase.compose.auth.composable

import androidx.compose.runtime.Composable
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.fallbackLogin
import io.github.jan.supabase.gotrue.SignOutScope
import io.github.jan.supabase.gotrue.providers.Google

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
expect fun ComposeAuth.rememberSignInWithGoogle(onResult: (NativeSignInResult) -> Unit = {}, fallback: suspend () -> Unit = { fallbackLogin(Google) }): NativeSignInState

/**
 * Composable function what implements Native Google Login flow with default fallback
 * @return [NativeSignInState]
 */
@Composable
@Deprecated("Use rememberSignInWithGoogle instead", ReplaceWith("rememberSignInWithGoogle(onResult, fallback)"), DeprecationLevel.WARNING)
fun ComposeAuth.rememberLoginWithGoogle(onResult: (NativeSignInResult) -> Unit = {}, fallback: suspend () -> Unit = { fallbackLogin(Google) }): NativeSignInState = rememberSignInWithGoogle(onResult, fallback)

/**
 * Composable for
 * @return [NativeSignInState]
 */
@Composable
expect fun ComposeAuth.rememberSignOutWithGoogle(signOutScope: SignOutScope = SignOutScope.LOCAL): NativeSignInState


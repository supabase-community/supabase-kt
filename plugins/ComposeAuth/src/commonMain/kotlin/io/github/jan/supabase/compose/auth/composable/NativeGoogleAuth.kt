package io.github.jan.supabase.compose.auth.composable

import androidx.compose.runtime.Composable
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.fallbackLogin
import io.github.jan.supabase.gotrue.SignOutScope
import io.github.jan.supabase.gotrue.providers.Google

/**
 * Composable function what implements Native Google Login flow with default fallback
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
 * Composable for sign out flow
 * @return [NativeSignInState]
 */
@Composable
expect fun ComposeAuth.rememberSignOutWithGoogle(signOutScope: SignOutScope = SignOutScope.LOCAL): NativeSignInState


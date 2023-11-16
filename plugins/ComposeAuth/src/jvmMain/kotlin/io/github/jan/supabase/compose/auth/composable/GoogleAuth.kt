package io.github.jan.supabase.compose.auth.composable

import androidx.compose.runtime.Composable
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.defaultLoginBehavior
import io.github.jan.supabase.compose.auth.defaultSignOutBehavior
import io.github.jan.supabase.gotrue.SignOutScope

/**
 * Composable for Google login with default behavior
 */
@Composable
actual fun ComposeAuth.rememberSignInWithGoogle(onResult: (NativeSignInResult) -> Unit, fallback: suspend () -> Unit): NativeSignInState = defaultLoginBehavior(fallback)

/**
 * Composable for SignOut with default behavior
 */
@Composable
actual fun ComposeAuth.rememberSignOutWithGoogle(signOutScope: SignOutScope): NativeSignInState = defaultSignOutBehavior(signOutScope)
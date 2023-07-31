package io.github.jan.supabase.compose.auth.composable

import androidx.compose.runtime.Composable
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.gotrue.LogoutScope

@Composable
actual fun ComposeAuth.rememberLoginWithGoogle(onResult: (NativeSignInResult) -> Unit, fallback: suspend () -> Unit): NativeSignInState = defaultLoginBehavior(fallback)

@Composable
actual fun ComposeAuth.rememberSignOut(logoutScope: LogoutScope): NativeSignInState = defaultSignOutBehavior(logoutScope)
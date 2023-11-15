package io.github.jan.supabase.compose.auth.composable

import androidx.compose.runtime.Composable
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.fallbackLogin
import io.github.jan.supabase.gotrue.providers.Apple

/**
 * Composable function that implements Native Auth flow for Apple login
 */
@Composable
expect fun ComposeAuth.rememberSignInWithApple(onResult: (NativeSignInResult) -> Unit, fallback: suspend () -> Unit = { fallbackLogin(Apple) }) : NativeSignInState

/**
 * Composable function that implements Native Auth flow for Apple login
 */
@Composable
@Deprecated("Use rememberSignInWithApple instead", ReplaceWith("rememberSignInWithApple(onResult, fallback)"), DeprecationLevel.WARNING)
fun ComposeAuth.rememberLoginWithApple(onResult: (NativeSignInResult) -> Unit, fallback: suspend () -> Unit = { fallbackLogin(Apple) }) : NativeSignInState = rememberSignInWithApple(onResult, fallback)
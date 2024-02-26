package io.github.jan.supabase.compose.auth.composable

import androidx.compose.runtime.Composable
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.defaultLoginBehavior

/**
 * Composable for Apple login with default behavior
 */
@Composable
actual fun ComposeAuth.rememberSignInWithApple(onResult: (NativeSignInResult) -> Unit, fallback: suspend () -> Unit): NativeSignInState = defaultLoginBehavior(fallback)
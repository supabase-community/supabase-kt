package io.github.jan.supabase.compose.auth.composable

import androidx.compose.runtime.Composable
import io.github.jan.supabase.compose.auth.ComposeAuth


/**
 * Composable for Apple login with default behavior
 */
@Composable
actual fun ComposeAuth.rememberLoginWithApple(onResult: (NativeSignInResult) -> Unit, fallback: suspend () -> Unit): NativeSignInState = defaultLoginBehavior(fallback)
package io.github.jan.supabase.composeAuth.composable

import androidx.compose.runtime.Composable
import io.github.jan.supabase.composeAuth.ComposeAuth

@Composable
expect fun ComposeAuth.rememberLoginWithGoogle(onResult: (NativeSignInResult) -> Unit = {}, fallback: () -> Unit = {}): NativeSignInState
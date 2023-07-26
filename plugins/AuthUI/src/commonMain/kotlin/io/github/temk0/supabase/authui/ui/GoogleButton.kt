package io.github.temk0.supabase.authui.ui

import androidx.compose.runtime.Composable
import io.github.temk0.supabase.authui.AuthUI

@Composable
expect fun AuthUI.loginWithGoogle(): () -> Unit
package io.github.jan.supabase.common.ui.components

import androidx.compose.runtime.Composable

@Composable
expect fun AlertDialog(text: String, close: () -> Unit)
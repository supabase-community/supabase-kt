package io.github.jan.supabase.common.ui.components

import androidx.compose.runtime.Composable
import kotlinx.browser.window

@Composable
actual fun AlertDialog(text: String, close: () -> Unit) {
    window.alert(text)
}
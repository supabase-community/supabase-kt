package io.github.jan.supabase.common.ui.components

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
actual fun AlertDialog(text: String, close: () -> Unit) {
    androidx.compose.material3.AlertDialog(
        text = { Text(text) },
        onDismissRequest = close,
        confirmButton = { Button(onClick = close) { Text("Ok") } }
    )
}
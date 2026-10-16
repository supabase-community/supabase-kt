package io.github.jan.supabase.common.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SecureTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordChangeDialog(
    onDismiss: () -> Unit,
    onConfirm: (newPassword: String) -> Unit
) {
    val passwordState = rememberTextFieldState()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Password change") },
        text = {
            Column {
                Text("Please enter your new password.")
                Spacer(Modifier.height(8.dp))
                SecureTextField(
                    state = passwordState
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(passwordState.text.toString())
                    onDismiss()
                },
                enabled = passwordState.text.isNotBlank()
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Dismiss")
            }
        }
    )
}
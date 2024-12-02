package io.github.jan.supabase.common.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.supabase.compose.auth.ui.AuthForm
import io.supabase.compose.auth.ui.LocalAuthState
import io.supabase.compose.auth.ui.annotations.AuthUiExperimental
import io.supabase.compose.auth.ui.password.OutlinedPasswordField

@OptIn(AuthUiExperimental::class, ExperimentalMaterial3Api::class)
@Composable
fun PasswordChangeDialog(
    onDismiss: () -> Unit,
    onConfirm: (newPassword: String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    AuthForm {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Password change") },
            text = {
                Column {
                    Text("Please enter your new password.")
                    Spacer(Modifier.height(8.dp))
                    OutlinedPasswordField(
                        value = password,
                        onValueChange = { password = it },
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onConfirm(password)
                        onDismiss()
                    },
                    enabled = LocalAuthState.current.validForm
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
}
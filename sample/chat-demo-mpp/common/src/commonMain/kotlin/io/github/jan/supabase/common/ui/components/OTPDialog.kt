package io.github.jan.supabase.common.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType

sealed interface OTPDialogState {
    data object Invisible : OTPDialogState
    data class Visible(val title: String = "Sign in using an OTP", val resetFlow: Boolean = false, val email: String? = null) : OTPDialogState
}

@Composable
fun OTPDialog(
    email: String? = null,
    title: String,
    onDismiss: () -> Unit,
    onConfirm: (email: String, code: String) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var otpEmail by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                if(email == null) {
                    OutlinedTextField(otpEmail, { otpEmail = it }, label = { Text("Email") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                } else {
                    Text("Please enter the code sent to $email.")
                }
                OutlinedTextField(code, { code = it }, label = { Text("Code") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(email ?: otpEmail, code)
                    onDismiss()
                },
                enabled = (email ?: otpEmail).isNotBlank() && code.isNotBlank()
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

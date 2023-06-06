package io.github.jan.supabase.common.ui.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordField(
    password: String,
    onPasswordChanged: (String) -> Unit,
    label: String = "Password",
    modifier: Modifier = Modifier,
    imeAction: ImeAction = ImeAction.Default,
    keyboardActions: KeyboardActions = KeyboardActions()
) {
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(
        password,
        onValueChange = onPasswordChanged,
        leadingIcon = { Icon(Icons.Filled.Key, "Password") },
        label = { Text(label) },
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = imeAction),
        keyboardActions = keyboardActions,
        singleLine = true,
        trailingIcon = {
            IconButton({
                visible = !visible
            }) {
                Icon(if(visible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, "", tint = MaterialTheme.colorScheme.onBackground)
            }
        },
        modifier = modifier
    )
}
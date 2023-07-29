package io.github.jan.supabase.compose.auth.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation

class PasswordRule(val description: String, val predicate: (password: String) -> Boolean) {

    companion object {

        fun minLength(length: Int, description: String = "Password must be at least $length characters long") = PasswordRule(description) {
            it.length >= length
        }

        fun containsLowercase(description: String = "Password must contain at least one lowercase character") = PasswordRule(description) {
            it.any { char -> char.isLowerCase() }
        }

        fun containsUppercase(description: String = "Password must contain at least one uppercase character") = PasswordRule(description) {
            it.any { char -> char.isUpperCase() }
        }

        fun containsDigit(description: String = "Password must contain at least one digit") = PasswordRule(description) {
            it.any { char -> char.isDigit() }
        }

        fun containsSpecialCharacter(description: String = "Password must contain at least one special character") = PasswordRule(description) {
            it.any { char -> char.isLetterOrDigit().not() }
        }

        fun maxLength(length: Int, description: String = "Password must be at most $length characters long") = PasswordRule(description) {
            it.length <= length
        }

    }

}

@Composable
fun rememberPasswordRuleList(vararg rules: PasswordRule) = remember { rules.toList() }

@ExperimentalMaterial3Api
@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    rules: List<PasswordRule> = rememberPasswordRuleList(),
    modifier: Modifier = Modifier,
    visualTransformation: (showPassword: Boolean) -> VisualTransformation = { if(it) VisualTransformation.None else PasswordVisualTransformation() },
    keyboardActions: KeyboardActions = KeyboardActions(),
    keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
    readOnly: Boolean = false,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    textStyle: TextStyle = LocalTextStyle.current,
    shape: Shape = TextFieldDefaults.filledShape,
    colors: TextFieldColors = TextFieldDefaults.textFieldColors(),
    label: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = { Icon(AuthIcons.rememberLockIcon(), "Lock") },
    supportingText: @Composable ((description: String?) -> Unit)? = { it?.let { Text(it) } },
    trailingIcon: @Composable ((showPassword: MutableState<Boolean>) -> Unit)? = { showPassword ->
        IconButton(onClick = { showPassword.value = !showPassword.value }) {
            Icon(if(showPassword.value) AuthIcons.rememberVisibilityIcon() else AuthIcons.rememberVisibilityOffIcon(), "Visibility")
        }
    },
    placeholder: @Composable (() -> Unit)? = null,
) {
    val showPassword = remember { mutableStateOf(false) }
    val errorMessage = remember(value, rules) { rules.firstOrNull { !it.predicate(value) }?.description }
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        visualTransformation = visualTransformation(showPassword.value),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        isError = errorMessage != null,
        modifier = modifier,
        leadingIcon = leadingIcon,
        trailingIcon = { trailingIcon?.invoke(showPassword) },
        supportingText = { supportingText?.invoke(errorMessage) },
        readOnly = readOnly,
        enabled = enabled,
        interactionSource = interactionSource,
        shape = shape,
        colors = colors,
        textStyle = textStyle,
        placeholder = placeholder,
    )
}

@ExperimentalMaterial3Api
@Composable
fun PasswordField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    rules: List<PasswordRule> = rememberPasswordRuleList(),
    modifier: Modifier = Modifier,
    visualTransformation: (showPassword: Boolean) -> VisualTransformation = { if(it) PasswordVisualTransformation() else VisualTransformation.None },
    keyboardActions: KeyboardActions = KeyboardActions(),
    keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
    readOnly: Boolean = false,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    textStyle: TextStyle = LocalTextStyle.current,
    shape: Shape = TextFieldDefaults.filledShape,
    colors: TextFieldColors = TextFieldDefaults.textFieldColors(),
    label: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = { Icon(AuthIcons.rememberLockIcon(), "Lock") },
    supportingText: @Composable ((description: String?) -> Unit)? = { it?.let { Text(it) } },
    trailingIcon: @Composable ((showPassword: MutableState<Boolean>) -> Unit)? = { showPassword ->
        IconButton(onClick = { showPassword.value = !showPassword.value }) {
            Icon(if(showPassword.value) AuthIcons.rememberVisibilityIcon() else AuthIcons.rememberVisibilityOffIcon(), "Visibility")
        }
    },
    placeholder: @Composable (() -> Unit)? = null,
) {
    val showPassword = remember { mutableStateOf(false) }
    val errorMessage = remember(value, rules) { rules.firstOrNull { !it.predicate(value.text) }?.description }
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        visualTransformation = visualTransformation(showPassword.value),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        isError = errorMessage != null,
        modifier = modifier,
        leadingIcon = leadingIcon,
        trailingIcon = { trailingIcon?.invoke(showPassword) },
        supportingText = { supportingText?.invoke(errorMessage) },
        readOnly = readOnly,
        enabled = enabled,
        interactionSource = interactionSource,
        shape = shape,
        colors = colors,
        textStyle = textStyle,
        placeholder = placeholder,
    )
}

@ExperimentalMaterial3Api
@Composable
fun OutlinedPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    rules: List<PasswordRule> = rememberPasswordRuleList(),
    modifier: Modifier = Modifier,
    visualTransformation: (showPassword: Boolean) -> VisualTransformation = { if(it) PasswordVisualTransformation() else VisualTransformation.None },
    keyboardActions: KeyboardActions = KeyboardActions(),
    keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
    readOnly: Boolean = false,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    textStyle: TextStyle = LocalTextStyle.current,
    shape: Shape = TextFieldDefaults.filledShape,
    colors: TextFieldColors = TextFieldDefaults.textFieldColors(),
    label: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = { Icon(AuthIcons.rememberLockIcon(), "Lock") },
    supportingText: @Composable ((description: String?) -> Unit)? = { it?.let { Text(it) } },
    trailingIcon: @Composable ((showPassword: MutableState<Boolean>) -> Unit)? = { showPassword ->
        IconButton(onClick = { showPassword.value = !showPassword.value }) {
            Icon(if(showPassword.value) AuthIcons.rememberVisibilityIcon() else AuthIcons.rememberVisibilityOffIcon(), "Visibility")
        }
    },
    placeholder: @Composable (() -> Unit)? = null,
) {
    val showPassword = remember { mutableStateOf(false) }
    val errorMessage = remember(value, rules) { rules.firstOrNull { !it.predicate(value) }?.description }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        visualTransformation = visualTransformation(showPassword.value),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        isError = errorMessage != null,
        modifier = modifier,
        leadingIcon = leadingIcon,
        trailingIcon = { trailingIcon?.invoke(showPassword) },
        supportingText = { supportingText?.invoke(errorMessage) },
        readOnly = readOnly,
        enabled = enabled,
        interactionSource = interactionSource,
        shape = shape,
        colors = colors,
        textStyle = textStyle,
        placeholder = placeholder,
    )
}

@ExperimentalMaterial3Api
@Composable
fun OutlinedPasswordField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    rules: List<PasswordRule> = rememberPasswordRuleList(),
    modifier: Modifier = Modifier,
    visualTransformation: (showPassword: Boolean) -> VisualTransformation = { if(it) PasswordVisualTransformation() else VisualTransformation.None },
    keyboardActions: KeyboardActions = KeyboardActions(),
    keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
    readOnly: Boolean = false,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    textStyle: TextStyle = LocalTextStyle.current,
    shape: Shape = TextFieldDefaults.filledShape,
    colors: TextFieldColors = TextFieldDefaults.textFieldColors(),
    label: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = { Icon(AuthIcons.rememberLockIcon(), "Lock") },
    supportingText: @Composable ((description: String?) -> Unit)? = { it?.let { Text(it) } },
    trailingIcon: @Composable ((showPassword: MutableState<Boolean>) -> Unit)? = { showPassword ->
        IconButton(onClick = { showPassword.value = !showPassword.value }) {
            Icon(if(showPassword.value) AuthIcons.rememberVisibilityIcon() else AuthIcons.rememberVisibilityOffIcon(), "Visibility")
        }
    },
    placeholder: @Composable (() -> Unit)? = null,
) {
    val showPassword = remember { mutableStateOf(false) }
    val errorMessage = remember(value, rules) { rules.firstOrNull { !it.predicate(value.text) }?.description }
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        visualTransformation = visualTransformation(showPassword.value),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        isError = errorMessage != null,
        modifier = modifier,
        leadingIcon = leadingIcon,
        trailingIcon = { trailingIcon?.invoke(showPassword) },
        supportingText = { supportingText?.invoke(errorMessage) },
        readOnly = readOnly,
        enabled = enabled,
        interactionSource = interactionSource,
        shape = shape,
        colors = colors,
        textStyle = textStyle,
        placeholder = placeholder,
    )
}
package io.github.jan.supabase.compose.auth.ui.password

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import io.github.jan.supabase.compose.auth.ui.AuthIcons
import io.github.jan.supabase.compose.auth.ui.LocalAuthState
import io.github.jan.supabase.compose.auth.ui.rememberLockIcon
import io.github.jan.supabase.compose.auth.ui.rememberVisibilityIcon
import io.github.jan.supabase.compose.auth.ui.rememberVisibilityOffIcon

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
    shape: Shape = TextFieldDefaults.shape,
    colors: TextFieldColors = TextFieldDefaults.colors(),
    label: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = { Icon(AuthIcons.rememberLockIcon(), "Lock") },
    supportingText: @Composable ((rules: List<PasswordRuleResult>) -> Unit)? = {
        if (value.isNotEmpty()) it.firstUnfulfilled()?.let { rule ->
            Text(rule.description)
        }
    },    trailingIcon: @Composable ((showPassword: MutableState<Boolean>) -> Unit)? = { showPassword ->
        IconButton(onClick = { showPassword.value = !showPassword.value }) {
            Icon(if(showPassword.value) AuthIcons.rememberVisibilityIcon() else AuthIcons.rememberVisibilityOffIcon(), "Visibility")
        }
    },
    placeholder: @Composable (() -> Unit)? = null,
    formKey: String = "PASSWORD"
) {
    val showPassword = remember { mutableStateOf(false) }
    val ruleResults = remember(value, rules) { rules.map { PasswordRuleResult(it.description, it.predicate(value)) } }
    val state = LocalAuthState.current
    LaunchedEffect(value, state) {
        state[formKey] = ruleResults.all { it.isFulfilled }
    }
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        visualTransformation = visualTransformation(showPassword.value),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        isError = ruleResults.firstUnfulfilled() != null && value.isNotEmpty(),
        modifier = modifier,
        leadingIcon = leadingIcon,
        trailingIcon = { trailingIcon?.invoke(showPassword) },
        supportingText = { supportingText?.invoke(ruleResults) },
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
    visualTransformation: (showPassword: Boolean) -> VisualTransformation = { if(it) VisualTransformation.None else PasswordVisualTransformation() },
    keyboardActions: KeyboardActions = KeyboardActions(),
    keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
    readOnly: Boolean = false,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    textStyle: TextStyle = LocalTextStyle.current,
    shape: Shape = TextFieldDefaults.shape,
    colors: TextFieldColors = TextFieldDefaults.colors(),
    label: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = { Icon(AuthIcons.rememberLockIcon(), "Lock") },
    supportingText: @Composable ((rules: List<PasswordRuleResult>) -> Unit)? = {
        if (value.text.isNotEmpty()) it.firstUnfulfilled()?.let { rule ->
            Text(rule.description)
        }
    },
    trailingIcon: @Composable ((showPassword: MutableState<Boolean>) -> Unit)? = { showPassword ->
        IconButton(onClick = { showPassword.value = !showPassword.value }) {
            Icon(if(showPassword.value) AuthIcons.rememberVisibilityIcon() else AuthIcons.rememberVisibilityOffIcon(), "Visibility")
        }
    },
    placeholder: @Composable (() -> Unit)? = null,
    formKey: String = "PASSWORD"
) {
    val showPassword = remember { mutableStateOf(false) }
    val ruleResults = remember(value, rules) { rules.map { PasswordRuleResult(it.description, it.predicate(value.text)) } }
    val state = LocalAuthState.current
    LaunchedEffect(value, state) {
        state[formKey] = ruleResults.all { it.isFulfilled }
    }
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        visualTransformation = visualTransformation(showPassword.value),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        isError = ruleResults.firstUnfulfilled() != null && value.text.isNotEmpty(),
        modifier = modifier,
        leadingIcon = leadingIcon,
        trailingIcon = { trailingIcon?.invoke(showPassword) },
        supportingText = { supportingText?.invoke(ruleResults) },
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
    visualTransformation: (showPassword: Boolean) -> VisualTransformation = { if(it) VisualTransformation.None else PasswordVisualTransformation() },
    keyboardActions: KeyboardActions = KeyboardActions(),
    keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
    readOnly: Boolean = false,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    textStyle: TextStyle = LocalTextStyle.current,
    shape: Shape = OutlinedTextFieldDefaults.shape,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
    label: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = { Icon(AuthIcons.rememberLockIcon(), "Lock") },
    supportingText: @Composable ((rules: List<PasswordRuleResult>) -> Unit)? = {
        if (value.isNotEmpty()) it.firstUnfulfilled()?.let { rule ->
            Text(rule.description)
        }
    },    trailingIcon: @Composable ((showPassword: MutableState<Boolean>) -> Unit)? = { showPassword ->
        IconButton(onClick = { showPassword.value = !showPassword.value }) {
            Icon(if(showPassword.value) AuthIcons.rememberVisibilityIcon() else AuthIcons.rememberVisibilityOffIcon(), "Visibility")
        }
    },
    placeholder: @Composable (() -> Unit)? = null,
    formKey: String = "PASSWORD"
) {
    val showPassword = remember { mutableStateOf(false) }
    val ruleResults = remember(value, rules) { rules.map { PasswordRuleResult(it.description, it.predicate(value)) } }
    val state = LocalAuthState.current
    LaunchedEffect(value, state) {
        state[formKey] = ruleResults.all { it.isFulfilled }
    }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        visualTransformation = visualTransformation(showPassword.value),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        isError = ruleResults.firstUnfulfilled() != null && value.isNotEmpty(),
        modifier = modifier,
        leadingIcon = leadingIcon,
        trailingIcon = { trailingIcon?.invoke(showPassword) },
        supportingText = { supportingText?.invoke(ruleResults) },
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
    visualTransformation: (showPassword: Boolean) -> VisualTransformation = { if(it) VisualTransformation.None else PasswordVisualTransformation() },
    keyboardActions: KeyboardActions = KeyboardActions(),
    keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
    readOnly: Boolean = false,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    textStyle: TextStyle = LocalTextStyle.current,
    shape: Shape = OutlinedTextFieldDefaults.shape,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
    label: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = { Icon(AuthIcons.rememberLockIcon(), "Lock") },
    supportingText: @Composable ((rules: List<PasswordRuleResult>) -> Unit)? = {
        if (value.text.isNotEmpty()) it.firstUnfulfilled()?.let { rule ->
            Text(rule.description)
        }
    }, trailingIcon: @Composable ((showPassword: MutableState<Boolean>) -> Unit)? = { showPassword ->
        IconButton(onClick = { showPassword.value = !showPassword.value }) {
            Icon(if(showPassword.value) AuthIcons.rememberVisibilityIcon() else AuthIcons.rememberVisibilityOffIcon(), "Visibility")
        }
    },
    placeholder: @Composable (() -> Unit)? = null,
    formKey: String = "PASSWORD"
) {
    val showPassword = remember { mutableStateOf(false) }
    val ruleResults = remember(value, rules) { rules.map { PasswordRuleResult(it.description, it.predicate(value.text)) } }
    val state = LocalAuthState.current
    LaunchedEffect(value, state) {
        state[formKey] = ruleResults.all { it.isFulfilled }
    }
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        visualTransformation = visualTransformation(showPassword.value),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        isError = ruleResults.firstUnfulfilled() != null && value.text.isNotEmpty(),
        modifier = modifier,
        leadingIcon = leadingIcon,
        trailingIcon = { trailingIcon?.invoke(showPassword) },
        supportingText = { supportingText?.invoke(ruleResults) },
        readOnly = readOnly,
        enabled = enabled,
        interactionSource = interactionSource,
        shape = shape,
        colors = colors,
        textStyle = textStyle,
        placeholder = placeholder,
    )
}

private fun List<PasswordRuleResult>.firstUnfulfilled(): PasswordRuleResult? = firstOrNull { !it.isFulfilled }
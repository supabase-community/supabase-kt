package io.github.jan.supabase.compose.auth.ui.email

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import io.github.jan.supabase.compose.auth.ui.AuthIcons
import io.github.jan.supabase.compose.auth.ui.FormValidator
import io.github.jan.supabase.compose.auth.ui.LocalAuthState
import io.github.jan.supabase.compose.auth.ui.rememberMailIcon

@ExperimentalMaterial3Api
@Composable
fun EmailField(
    value: String,
    onValueChange: (String) -> Unit,
    validator: FormValidator = FormValidator.EMAIL,
    modifier: Modifier = Modifier,
    label: (@Composable () -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Email
    ),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    leadingIcon: (@Composable () -> Unit)? = {
        Icon(
            imageVector = AuthIcons.rememberMailIcon(),
            contentDescription = "Email",
        )
    },
    singleLine: Boolean = true,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    textStyle: TextStyle = LocalTextStyle.current,
    shape: Shape = TextFieldDefaults.shape,
    colors: TextFieldColors = TextFieldDefaults.colors(),
    supportingText: @Composable ((validEmail: Boolean) -> Unit)? = { if(!it) Text("Please enter a valid email address") },
    trailingIcon: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    formKey: String = "EMAIL",
) {
    val isValidEmail = remember(value) { validator.validate(value) }
    val state = LocalAuthState.current
    LaunchedEffect(isValidEmail) {
        state[formKey] = isValidEmail
    }
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        leadingIcon = leadingIcon,
        singleLine = singleLine,
        isError = !isValidEmail && value.isNotEmpty(),
        interactionSource = interactionSource,
        textStyle = textStyle,
        shape = shape,
        colors = colors,
        supportingText = {
            supportingText?.invoke(isValidEmail || value.isBlank())
        },
        trailingIcon = trailingIcon,
        placeholder = placeholder,
        enabled = enabled,
    )
}

@ExperimentalMaterial3Api
@Composable
fun EmailField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    validator: FormValidator = FormValidator.EMAIL,
    modifier: Modifier = Modifier,
    label: (@Composable () -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Email
    ),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    leadingIcon: (@Composable () -> Unit)? = {
        Icon(
            imageVector = AuthIcons.rememberMailIcon(),
            contentDescription = "Email",
        )
    },
    singleLine: Boolean = true,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    textStyle: TextStyle = LocalTextStyle.current,
    shape: Shape = TextFieldDefaults.shape,
    colors: TextFieldColors = TextFieldDefaults.colors(),
    supportingText: @Composable ((validEmail: Boolean) -> Unit)? = { if(!it) Text("Please enter a valid email address") },
    trailingIcon: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    formKey: String = "EMAIL",
) {
    val isValidEmail = remember(value) { validator.validate(value.text) }
    val state = LocalAuthState.current
    LaunchedEffect(isValidEmail) {
        state[formKey] = isValidEmail
    }
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        leadingIcon = leadingIcon,
        singleLine = singleLine,
        isError = !isValidEmail && value.text.isNotEmpty(),
        interactionSource = interactionSource,
        textStyle = textStyle,
        shape = shape,
        colors = colors,
        supportingText = {
            supportingText?.invoke(isValidEmail || value.text.isBlank())
        },
        trailingIcon = trailingIcon,
        placeholder = placeholder,
        enabled = enabled,
    )
}

@ExperimentalMaterial3Api
@Composable
fun OutlinedEmailField(
    value: String,
    onValueChange: (String) -> Unit,
    validator: FormValidator = FormValidator.EMAIL,
    modifier: Modifier = Modifier,
    label: (@Composable () -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Email
    ),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    leadingIcon: (@Composable () -> Unit)? = {
        Icon(
            imageVector = AuthIcons.rememberMailIcon(),
            contentDescription = "Email",
        )
    },
    singleLine: Boolean = true,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    textStyle: TextStyle = LocalTextStyle.current,
    shape: Shape = OutlinedTextFieldDefaults.shape,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
    supportingText: @Composable ((validEmail: Boolean) -> Unit)? = { if(!it) Text("Please enter a valid email address") },
    trailingIcon: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    formKey: String = "EMAIL",
) {
    val isValidEmail = remember(value) { validator.validate(value) }
    val state = LocalAuthState.current
    LaunchedEffect(isValidEmail) {
        state[formKey] = isValidEmail
    }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        leadingIcon = leadingIcon,
        singleLine = singleLine,
        isError = !isValidEmail && value.isNotEmpty(),
        interactionSource = interactionSource,
        textStyle = textStyle,
        shape = shape,
        colors = colors,
        supportingText = {
            supportingText?.invoke(isValidEmail || value.isBlank())
        },
        trailingIcon = trailingIcon,
        placeholder = placeholder,
        enabled = enabled,
    )
}

@ExperimentalMaterial3Api
@Composable
fun OutlinedEmailField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    validator: FormValidator = FormValidator.EMAIL,
    modifier: Modifier = Modifier,
    label: (@Composable () -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Email
    ),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    leadingIcon: (@Composable () -> Unit)? = {
        Icon(
            imageVector = AuthIcons.rememberMailIcon(),
            contentDescription = "Email",
        )
    },
    singleLine: Boolean = true,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    textStyle: TextStyle = LocalTextStyle.current,
    shape: Shape = OutlinedTextFieldDefaults.shape,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
    supportingText: @Composable ((validEmail: Boolean) -> Unit)? = { if(!it) Text("Please enter a valid email address") },
    trailingIcon: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    formKey: String = "EMAIL",
) {
    val isValidEmail = remember(value) { validator.validate(value.text) }
    val state = LocalAuthState.current
    LaunchedEffect(isValidEmail) {
        state[formKey] = isValidEmail
    }
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        leadingIcon = leadingIcon,
        singleLine = singleLine,
        isError = !isValidEmail && value.text.isNotEmpty(),
        interactionSource = interactionSource,
        textStyle = textStyle,
        shape = shape,
        colors = colors,
        supportingText = {
            supportingText?.invoke(isValidEmail || value.text.isBlank())
        },
        trailingIcon = trailingIcon,
        placeholder = placeholder,
        enabled = enabled,
    )
}
package io.github.jan.supabase.compose.auth.ui.phone

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.VisualTransformation
import io.github.jan.supabase.compose.auth.ui.AuthIcons
import io.github.jan.supabase.compose.auth.ui.FormValidator
import io.github.jan.supabase.compose.auth.ui.LocalAuthState
import io.github.jan.supabase.compose.auth.ui.rememberCallIcon

@Composable
fun PhoneField(
    value: String,
    onValueChange: (String) -> Unit,
    validator: FormValidator = remember { FormValidator.PHONE },
    mask: String? = "+## ### #########",
    maskChar: Char = '#',
    modifier: Modifier = Modifier,
    label: (@Composable () -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Phone
    ),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    leadingIcon: (@Composable () -> Unit)? = {
        Icon(
            imageVector = AuthIcons.rememberCallIcon(),
            contentDescription = "Phone",
        )
    },
    singleLine: Boolean = true,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    textStyle: TextStyle = LocalTextStyle.current,
    shape: Shape = TextFieldDefaults.shape,
    colors: TextFieldColors = TextFieldDefaults.colors(),
    visualTransformation: VisualTransformation = mask?.let { PhoneVisualTransformation(it, maskChar) } ?: VisualTransformation.None,
    supportingText: @Composable ((validPhone: Boolean) -> Unit)? = { if(!it) Text("Please enter a valid phone number") },
    trailingIcon: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    formKey: String = "PHONE",
) {
    val isValidPhone = remember(value) { validator.validate(value) }
    val state = LocalAuthState.current
    LaunchedEffect(isValidPhone) {
        state[formKey] = isValidPhone
    }
    TextField(
        value = value,
        onValueChange = { onValueChange(it.filter { char -> char.isDigit() }.take(mask?.count { c -> c == maskChar } ?: it.length)) },
        modifier = modifier,
        label = label,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        leadingIcon = leadingIcon,
        singleLine = singleLine,
        isError = !isValidPhone && value.isNotEmpty(),
        interactionSource = interactionSource,
        textStyle = textStyle,
        shape = shape,
        colors = colors,
        supportingText = {
            supportingText?.invoke(isValidPhone || value.isBlank())
        },
        visualTransformation = visualTransformation,
        trailingIcon = trailingIcon,
        placeholder = placeholder,
        enabled = enabled,
    )
}

@Composable
fun PhoneField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    validator: FormValidator = remember { FormValidator.PHONE },
    mask: String? = "+## ### #########",
    maskChar: Char = '#',
    modifier: Modifier = Modifier,
    label: (@Composable () -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Phone
    ),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    leadingIcon: (@Composable () -> Unit)? = {
        Icon(
            imageVector = AuthIcons.rememberCallIcon(),
            contentDescription = "Phone",
        )
    },
    singleLine: Boolean = true,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    textStyle: TextStyle = LocalTextStyle.current,
    shape: Shape = TextFieldDefaults.shape,
    colors: TextFieldColors = TextFieldDefaults.colors(),
    visualTransformation: VisualTransformation = mask?.let { PhoneVisualTransformation(it, maskChar) } ?: VisualTransformation.None,
    supportingText: @Composable ((validPhone: Boolean) -> Unit)? = { if(!it) Text("Please enter a valid phone number") },
    trailingIcon: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    formKey: String = "PHONE",
) {
    val isValidPhone = remember(value) { validator.validate(value.text) }
    val state = LocalAuthState.current
    LaunchedEffect(isValidPhone) {
        state[formKey] = isValidPhone
    }
    TextField(
        value = value,
        onValueChange = { onValueChange(it.copy(it.text.filter { char -> char.isDigit() }.take(mask?.count { c -> c == maskChar }
            ?: it.text.length))) },
        modifier = modifier,
        label = label,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        leadingIcon = leadingIcon,
        singleLine = singleLine,
        isError = !isValidPhone && value.text.isNotEmpty(),
        interactionSource = interactionSource,
        textStyle = textStyle,
        shape = shape,
        colors = colors,
        supportingText = {
            supportingText?.invoke(isValidPhone || value.text.isBlank())
        },
        visualTransformation = visualTransformation,
        trailingIcon = trailingIcon,
        placeholder = placeholder,
        enabled = enabled,
    )
}

@Composable
fun OutlinedPhoneField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    validator: FormValidator = remember { FormValidator.PHONE },
    mask: String? = "+## ### #########",
    maskChar: Char = '#',
    modifier: Modifier = Modifier,
    label: (@Composable () -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Phone
    ),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    leadingIcon: (@Composable () -> Unit)? = {
        Icon(
            imageVector = AuthIcons.rememberCallIcon(),
            contentDescription = "Phone",
        )
    },
    singleLine: Boolean = true,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    textStyle: TextStyle = LocalTextStyle.current,
    shape: Shape = OutlinedTextFieldDefaults.shape,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
    visualTransformation: VisualTransformation = mask?.let { PhoneVisualTransformation(it, maskChar) } ?: VisualTransformation.None,
    supportingText: @Composable ((validPhone: Boolean) -> Unit)? = { if(!it) Text("Please enter a valid phone number") },
    trailingIcon: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    formKey: String = "PHONE",
) {
    val isValidPhone = remember(value) { validator.validate(value.text) }
    val state = LocalAuthState.current
    LaunchedEffect(isValidPhone) {
        state[formKey] = isValidPhone
    }
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it.copy(it.text.filter { char -> char.isDigit() }.take(mask?.count { c -> c == maskChar } ?: it.text.length))) },
        modifier = modifier,
        label = label,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        leadingIcon = leadingIcon,
        singleLine = singleLine,
        isError = !isValidPhone && value.text.isNotEmpty(),
        interactionSource = interactionSource,
        textStyle = textStyle,
        shape = shape,
        colors = colors,
        supportingText = {
            supportingText?.invoke(isValidPhone || value.text.isBlank())
        },
        visualTransformation = visualTransformation,
        trailingIcon = trailingIcon,
        placeholder = placeholder,
        enabled = enabled,
    )
}

@Composable
fun OutlinedPhoneField(
    value: String,
    onValueChange: (String) -> Unit,
    validator: FormValidator = remember { FormValidator.PHONE },
    mask: String? = "+## ### #########",
    maskChar: Char = '#',
    modifier: Modifier = Modifier,
    label: (@Composable () -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Phone
    ),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    leadingIcon: (@Composable () -> Unit)? = {
        Icon(
            imageVector = AuthIcons.rememberCallIcon(),
            contentDescription = "Phone",
        )
    },
    singleLine: Boolean = true,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    textStyle: TextStyle = LocalTextStyle.current,
    shape: Shape = OutlinedTextFieldDefaults.shape,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
    visualTransformation: VisualTransformation = mask?.let { PhoneVisualTransformation(it, maskChar) } ?: VisualTransformation.None,
    supportingText: @Composable ((validPhone: Boolean) -> Unit)? = { if(!it) Text("Please enter a valid phone number") },
    trailingIcon: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    formKey: String = "PHONE",
) {
    val isValidPhone = remember(value) { validator.validate(value) }
    val state = LocalAuthState.current
    LaunchedEffect(isValidPhone) {
        state[formKey] = isValidPhone
    }
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it.filter { char -> char.isDigit() }.take(mask?.count { it == maskChar } ?: it.length)) },
        modifier = modifier,
        label = label,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        leadingIcon = leadingIcon,
        singleLine = singleLine,
        isError = !isValidPhone && value.isNotEmpty(),
        interactionSource = interactionSource,
        textStyle = textStyle,
        shape = shape,
        colors = colors,
        supportingText = {
            supportingText?.invoke(isValidPhone || value.isBlank())
        },
        visualTransformation = visualTransformation,
        trailingIcon = trailingIcon,
        placeholder = placeholder,
        enabled = enabled,
    )
}
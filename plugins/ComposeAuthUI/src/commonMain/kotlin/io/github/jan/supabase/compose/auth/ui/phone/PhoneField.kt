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
import io.github.jan.supabase.compose.auth.ui.FormComponent
import io.github.jan.supabase.compose.auth.ui.FormValidator
import io.github.jan.supabase.compose.auth.ui.annotations.AuthUiExperimental
import io.github.jan.supabase.compose.auth.ui.rememberCallIcon

private const val DEFAULT_MASK = "+## ### #########"
private const val DEFAULT_MASK_CHAR = '#'

/**
 * A custom email input field with validation and pre-defined styling.
 *
 * @param value The current value of the phone field.
 * @param onValueChange The callback function for when the value of the phone field changes.
 * @param validator The form validator used to validate the phone field value. Defaults to [FormValidator.PHONE].
 * @param mask The mask for the phone field. This changes how the phone number gets displayed. The value will still be the raw number. Defaults to "+## ### #########".
 * @param maskChar The mask character for the phone field. Defaults to '#'.
 * @param modifier The modifier for styling the phone field. Defaults to Modifier.
 * @param label The label for the phone field. Defaults to null.
 * @param keyboardOptions The keyboard options for the phone field. Defaults to KeyboardOptions(keyboardType = KeyboardType.Email).
 * @param keyboardActions The keyboard actions for the phone field. Defaults to KeyboardActions.Default.
 * @param leadingIcon The leading icon for the phone field. Defaults to an email icon.
 * @param singleLine Whether the phone field should be a single line or multiline. Defaults to true.
 * @param enabled Whether the phone field should be enabled for user interaction. Defaults to true.
 * @param isError Whether the phone field should display an error state. Defaults to null (handled automatically).
 * @param interactionSource The interaction source for the phone field. Defaults to MutableInteractionSource.
 * @param textStyle The text style for the phone field. Defaults to LocalTextStyle.current.
 * @param shape The shape of the phone field. Defaults to TextFieldDefaults.shape.
 * @param colors The colors for the phone field. Defaults to TextFieldDefaults.colors().
 * @param supportingText A composable function to display supporting text based on the validity of the phone field value. Defaults to displaying "Please enter a valid email address" if the value is not a valid email.
 * @param trailingIcon The trailing icon for the phone field. Defaults to null.
 * @param placeholder The placeholder for the phone field. Defaults to null.
 * @param formKey The key to store the validity of the phone field in the AuthState. Defaults to "EMAIL".
 * @param mandatory Whether the form field is mandatory or not. If false, will not affect the [AuthState.validForm] value. You can also make this value dynamic and only make the field mandatory, if e.g. the [value] is not empty. Default is true.
 */
@AuthUiExperimental
@Composable
fun PhoneField(
    value: String,
    onValueChange: (String) -> Unit,
    validator: FormValidator = remember { FormValidator.PHONE },
    mask: String? = DEFAULT_MASK,
    maskChar: Char = DEFAULT_MASK_CHAR,
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
    isError: Boolean? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    textStyle: TextStyle = LocalTextStyle.current,
    shape: Shape = TextFieldDefaults.shape,
    colors: TextFieldColors = TextFieldDefaults.colors(),
    visualTransformation: VisualTransformation = mask?.let { PhoneVisualTransformation(it, maskChar) } ?: VisualTransformation.None,
    supportingText: @Composable ((validPhone: Boolean) -> Unit)? = { if(!it) Text("Please enter a valid phone number") },
    trailingIcon: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    formKey: String = "PHONE",
    mandatory: Boolean = true
) {
    FormComponent(formKey, mandatory) {
        val isValidPhone = remember(value) { validator.validate(value) }
        LaunchedEffect(isValidPhone) {
            it.value = isValidPhone
        }
        TextField(
            value = value,
            onValueChange = {
                onValueChange(it.filter { char -> char.isDigit() }
                    .take(mask?.count { c -> c == maskChar } ?: it.length))
            },
            modifier = modifier,
            label = label,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            leadingIcon = leadingIcon,
            singleLine = singleLine,
            isError = isError ?: (!isValidPhone && value.isNotEmpty()),
            interactionSource = interactionSource,
            textStyle = textStyle,
            shape = shape,
            colors = colors,
            supportingText = { supportingText?.invoke(isValidPhone) },
            visualTransformation = visualTransformation,
            trailingIcon = trailingIcon,
            placeholder = placeholder,
            enabled = enabled,
        )
    }
}

/**
 * A custom email input field with validation and pre-defined styling.
 *
 * @param value The current value of the phone field.
 * @param onValueChange The callback function for when the value of the phone field changes.
 * @param validator The form validator used to validate the phone field value. Defaults to [FormValidator.PHONE].
 * @param mask The mask for the phone field. This changes how the phone number gets displayed. The value will still be the raw number. Defaults to "+## ### #########".
 * @param maskChar The mask character for the phone field. Defaults to '#'.
 * @param modifier The modifier for styling the phone field. Defaults to Modifier.
 * @param label The label for the phone field. Defaults to null.
 * @param keyboardOptions The keyboard options for the phone field. Defaults to KeyboardOptions(keyboardType = KeyboardType.Email).
 * @param keyboardActions The keyboard actions for the phone field. Defaults to KeyboardActions.Default.
 * @param leadingIcon The leading icon for the phone field. Defaults to an email icon.
 * @param singleLine Whether the phone field should be a single line or multiline. Defaults to true.
 * @param isError Whether the phone field should display an error state. Defaults to null (handled automatically).
 * @param enabled Whether the phone field should be enabled for user interaction. Defaults to true.
 * @param interactionSource The interaction source for the phone field. Defaults to MutableInteractionSource.
 * @param textStyle The text style for the phone field. Defaults to LocalTextStyle.current.
 * @param shape The shape of the phone field. Defaults to TextFieldDefaults.shape.
 * @param colors The colors for the phone field. Defaults to TextFieldDefaults.colors().
 * @param supportingText A composable function to display supporting text based on the validity of the phone field value. Defaults to displaying "Please enter a valid email address" if the value is not a valid email.
 * @param trailingIcon The trailing icon for the phone field. Defaults to null.
 * @param placeholder The placeholder for the phone field. Defaults to null.
 * @param formKey The key to store the validity of the phone field in the AuthState. Defaults to "EMAIL".
 * @param mandatory Whether the form field is mandatory or not. If false, will not affect the [AuthState.validForm] value. You can also make this value dynamic and only make the field mandatory, if e.g. the [value] is not empty. Default is true.
 */
@AuthUiExperimental
@Composable
fun PhoneField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    validator: FormValidator = remember { FormValidator.PHONE },
    mask: String? = DEFAULT_MASK,
    maskChar: Char = DEFAULT_MASK_CHAR,
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
    isError: Boolean? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    textStyle: TextStyle = LocalTextStyle.current,
    shape: Shape = TextFieldDefaults.shape,
    colors: TextFieldColors = TextFieldDefaults.colors(),
    visualTransformation: VisualTransformation = mask?.let { PhoneVisualTransformation(it, maskChar) } ?: VisualTransformation.None,
    supportingText: @Composable ((validPhone: Boolean) -> Unit)? = { if(!it) Text("Please enter a valid phone number") },
    trailingIcon: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    formKey: String = "PHONE",
    mandatory: Boolean = true
) {
    FormComponent(formKey, mandatory) {
        val isValidPhone = remember(value) { validator.validate(value.text) }
        LaunchedEffect(isValidPhone) {
            it.value = isValidPhone
        }
        TextField(
            value = value,
            onValueChange = {
                onValueChange(it.copy(it.text.filter { char -> char.isDigit() }.take(mask?.count { c -> c == maskChar }
                    ?: it.text.length)))
            },
            modifier = modifier,
            label = label,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            leadingIcon = leadingIcon,
            singleLine = singleLine,
            isError = isError ?: (!isValidPhone && value.text.isNotEmpty()),
            interactionSource = interactionSource,
            textStyle = textStyle,
            shape = shape,
            colors = colors,
            supportingText = { supportingText?.invoke(isValidPhone) },
            visualTransformation = visualTransformation,
            trailingIcon = trailingIcon,
            placeholder = placeholder,
            enabled = enabled,
        )
    }
}

/**
 * A custom email input field with validation and pre-defined styling.
 *
 * @param value The current value of the phone field.
 * @param onValueChange The callback function for when the value of the phone field changes.
 * @param validator The form validator used to validate the phone field value. Defaults to [FormValidator.PHONE].
 * @param mask The mask for the phone field. This changes how the phone number gets displayed. The value will still be the raw number. Defaults to "+## ### #########".
 * @param maskChar The mask character for the phone field. Defaults to '#'.
 * @param modifier The modifier for styling the phone field. Defaults to Modifier.
 * @param label The label for the phone field. Defaults to null.
 * @param keyboardOptions The keyboard options for the phone field. Defaults to KeyboardOptions(keyboardType = KeyboardType.Email).
 * @param keyboardActions The keyboard actions for the phone field. Defaults to KeyboardActions.Default.
 * @param leadingIcon The leading icon for the phone field. Defaults to an email icon.
 * @param singleLine Whether the phone field should be a single line or multiline. Defaults to true.
 * @param enabled Whether the phone field should be enabled for user interaction. Defaults to true.
 * @param isError Whether the phone field should display an error state. Defaults to null (handled automatically).
 * @param interactionSource The interaction source for the phone field. Defaults to MutableInteractionSource.
 * @param textStyle The text style for the phone field. Defaults to LocalTextStyle.current.
 * @param shape The shape of the phone field. Defaults to TextFieldDefaults.shape.
 * @param colors The colors for the phone field. Defaults to TextFieldDefaults.colors().
 * @param supportingText A composable function to display supporting text based on the validity of the phone field value. Defaults to displaying "Please enter a valid email address" if the value is not a valid email.
 * @param trailingIcon The trailing icon for the phone field. Defaults to null.
 * @param placeholder The placeholder for the phone field. Defaults to null.
 * @param formKey The key to store the validity of the phone field in the AuthState. Defaults to "EMAIL".
 * @param mandatory Whether the form field is mandatory or not. If false, will not affect the [AuthState.validForm] value. You can also make this value dynamic and only make the field mandatory, if e.g. the [value] is not empty. Default is true.
 */
@AuthUiExperimental
@Composable
fun OutlinedPhoneField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    validator: FormValidator = remember { FormValidator.PHONE },
    mask: String? = DEFAULT_MASK,
    maskChar: Char = DEFAULT_MASK_CHAR,
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
    isError: Boolean? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    textStyle: TextStyle = LocalTextStyle.current,
    shape: Shape = OutlinedTextFieldDefaults.shape,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
    visualTransformation: VisualTransformation = mask?.let { PhoneVisualTransformation(it, maskChar) } ?: VisualTransformation.None,
    supportingText: @Composable ((validPhone: Boolean) -> Unit)? = { if(!it) Text("Please enter a valid phone number") },
    trailingIcon: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    formKey: String = "PHONE",
    mandatory: Boolean = true
) {
    FormComponent(formKey, mandatory) {
        val isValidPhone = remember(value) { validator.validate(value.text) }
        LaunchedEffect(isValidPhone) {
            it.value = isValidPhone
        }
        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(it.copy(it.text.filter { char -> char.isDigit() }
                    .take(mask?.count { c -> c == maskChar } ?: it.text.length)))
            },
            modifier = modifier,
            label = label,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            leadingIcon = leadingIcon,
            singleLine = singleLine,
            isError = isError ?: (!isValidPhone && value.text.isNotEmpty()),
            interactionSource = interactionSource,
            textStyle = textStyle,
            shape = shape,
            colors = colors,
            supportingText = { supportingText?.invoke(isValidPhone) },
            visualTransformation = visualTransformation,
            trailingIcon = trailingIcon,
            placeholder = placeholder,
            enabled = enabled,
        )
    }
}

/**
 * A custom email input field with validation and pre-defined styling.
 *
 * @param value The current value of the phone field.
 * @param onValueChange The callback function for when the value of the phone field changes.
 * @param validator The form validator used to validate the phone field value. Defaults to [FormValidator.PHONE].
 * @param mask The mask for the phone field. This changes how the phone number gets displayed. The value will still be the raw number. Defaults to "+## ### #########".
 * @param maskChar The mask character for the phone field. Defaults to '#'.
 * @param modifier The modifier for styling the phone field. Defaults to Modifier.
 * @param label The label for the phone field. Defaults to null.
 * @param keyboardOptions The keyboard options for the phone field. Defaults to KeyboardOptions(keyboardType = KeyboardType.Email).
 * @param keyboardActions The keyboard actions for the phone field. Defaults to KeyboardActions.Default.
 * @param leadingIcon The leading icon for the phone field. Defaults to an email icon.
 * @param singleLine Whether the phone field should be a single line or multiline. Defaults to true.
 * @param enabled Whether the phone field should be enabled for user interaction. Defaults to true.
 * @param isError Whether the phone field should display an error state. Defaults to null (handled automatically).
 * @param interactionSource The interaction source for the phone field. Defaults to MutableInteractionSource.
 * @param textStyle The text style for the phone field. Defaults to LocalTextStyle.current.
 * @param shape The shape of the phone field. Defaults to TextFieldDefaults.shape.
 * @param colors The colors for the phone field. Defaults to TextFieldDefaults.colors().
 * @param supportingText A composable function to display supporting text based on the validity of the phone field value. Defaults to displaying "Please enter a valid email address" if the value is not a valid email.
 * @param trailingIcon The trailing icon for the phone field. Defaults to null.
 * @param placeholder The placeholder for the phone field. Defaults to null.
 * @param formKey The key to store the validity of the phone field in the AuthState. Defaults to "EMAIL".
 * @param mandatory Whether the form field is mandatory or not. If false, will not affect the [AuthState.validForm] value. You can also make this value dynamic and only make the field mandatory, if e.g. the [value] is not empty. Default is true.
 */
@AuthUiExperimental
@Composable
fun OutlinedPhoneField(
    value: String,
    onValueChange: (String) -> Unit,
    validator: FormValidator = remember { FormValidator.PHONE },
    mask: String? = DEFAULT_MASK,
    maskChar: Char = DEFAULT_MASK_CHAR,
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
    isError: Boolean? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    textStyle: TextStyle = LocalTextStyle.current,
    shape: Shape = OutlinedTextFieldDefaults.shape,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
    visualTransformation: VisualTransformation = mask?.let { PhoneVisualTransformation(it, maskChar) } ?: VisualTransformation.None,
    supportingText: @Composable ((validPhone: Boolean) -> Unit)? = { if(!it) Text("Please enter a valid phone number") },
    trailingIcon: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    formKey: String = "PHONE",
    mandatory: Boolean = true
) {
    FormComponent(formKey, mandatory) {
        val isValidPhone = remember(value) { validator.validate(value) }
        LaunchedEffect(isValidPhone) {
            it.value = isValidPhone
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
            isError = isError ?: (!isValidPhone && value.isNotEmpty()),
            interactionSource = interactionSource,
            textStyle = textStyle,
            shape = shape,
            colors = colors,
            supportingText = { supportingText?.invoke(isValidPhone) },
            visualTransformation = visualTransformation,
            trailingIcon = trailingIcon,
            placeholder = placeholder,
            enabled = enabled,
        )
    }
}
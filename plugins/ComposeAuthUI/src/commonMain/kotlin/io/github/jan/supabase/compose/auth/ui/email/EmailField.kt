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
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.compose.auth.ui.AuthIcons
import io.github.jan.supabase.compose.auth.ui.FormComponent
import io.github.jan.supabase.compose.auth.ui.FormValidator
import io.github.jan.supabase.compose.auth.ui.rememberMailIcon

/**
 * A custom email input field with validation and pre-defined styling.
 *
 * @param value The current value of the email field.
 * @param onValueChange The callback function for when the value of the email field changes.
 * @param validator The form validator used to validate the email field value. Defaults to [FormValidator.EMAIL].
 * @param modifier The modifier for styling the email field. Defaults to Modifier.
 * @param label The label for the email field. Defaults to null.
 * @param keyboardOptions The keyboard options for the email field. Defaults to KeyboardOptions(keyboardType = KeyboardType.Email).
 * @param keyboardActions The keyboard actions for the email field. Defaults to KeyboardActions.Default.
 * @param leadingIcon The leading icon for the email field. Defaults to an email icon.
 * @param singleLine Whether the email field should be a single line or multiline. Defaults to true.
 * @param enabled Whether the email field should be enabled for user interaction. Defaults to true.
 * @param interactionSource The interaction source for the email field. Defaults to MutableInteractionSource.
 * @param textStyle The text style for the email field. Defaults to LocalTextStyle.current.
 * @param shape The shape of the email field. Defaults to TextFieldDefaults.shape.
 * @param colors The colors for the email field. Defaults to TextFieldDefaults.colors().
 * @param supportingText A composable function to display supporting text based on the validity of the email field value. Defaults to displaying "Please enter a valid email address" if the value is not a valid email.
 * @param trailingIcon The trailing icon for the email field. Defaults to null.
 * @param placeholder The placeholder for the email field. Defaults to null.
 * @param formKey The key to store the validity of the email field in the AuthState. Defaults to "EMAIL".
 * @param mandatory Whether the form field is mandatory or not. If false, will not affect the [AuthState.validForm] value. You can also make this value dynamic and only make the field mandatory, if e.g. the [value] is not empty. Default is true.
 */
@ExperimentalMaterial3Api
@SupabaseExperimental
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
    mandatory: Boolean = true,
) {
    FormComponent(formKey, mandatory) {
        val isValidEmail = remember(value) { validator.validate(value) }
        LaunchedEffect(isValidEmail) {
            it.value = isValidEmail
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
            supportingText = { supportingText?.invoke(isValidEmail || value.isEmpty()) },
            trailingIcon = trailingIcon,
            placeholder = placeholder,
            enabled = enabled,
        )
    }
}

/**
 * A custom email input field with validation and pre-defined styling.
 *
 * @param value The current value of the email field.
 * @param onValueChange The callback function for when the value of the email field changes.
 * @param validator The form validator used to validate the email field value. Defaults to [FormValidator.EMAIL].
 * @param modifier The modifier for styling the email field. Defaults to Modifier.
 * @param label The label for the email field. Defaults to null.
 * @param keyboardOptions The keyboard options for the email field. Defaults to KeyboardOptions(keyboardType = KeyboardType.Email).
 * @param keyboardActions The keyboard actions for the email field. Defaults to KeyboardActions.Default.
 * @param leadingIcon The leading icon for the email field. Defaults to an email icon.
 * @param singleLine Whether the email field should be a single line or multiline. Defaults to true.
 * @param enabled Whether the email field should be enabled for user interaction. Defaults to true.
 * @param interactionSource The interaction source for the email field. Defaults to MutableInteractionSource.
 * @param textStyle The text style for the email field. Defaults to LocalTextStyle.current.
 * @param shape The shape of the email field. Defaults to TextFieldDefaults.shape.
 * @param colors The colors for the email field. Defaults to TextFieldDefaults.colors().
 * @param supportingText A composable function to display supporting text based on the validity of the email field value. Defaults to displaying "Please enter a valid email address" if the value is not a valid email.
 * @param trailingIcon The trailing icon for the email field. Defaults to null.
 * @param placeholder The placeholder for the email field. Defaults to null.
 * @param formKey The key to store the validity of the email field in the AuthState. Defaults to "EMAIL".
 * @param mandatory Whether the form field is mandatory or not. If false, will not affect the [AuthState.validForm] value. You can also make this value dynamic and only make the field mandatory, if e.g. the [value] is not empty. Default is true.
 */
@SupabaseExperimental
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
    mandatory: Boolean = true,
) {
    FormComponent(formKey, mandatory) {
        val isValidEmail = remember(value) { validator.validate(value.text) }
        LaunchedEffect(isValidEmail) {
            it.value = isValidEmail
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
            supportingText = { supportingText?.invoke(isValidEmail || value.text.isEmpty()) },
            trailingIcon = trailingIcon,
            placeholder = placeholder,
            enabled = enabled,
        )
    }
}

/**
 * A custom email input field with validation and pre-defined styling.
 *
 * @param value The current value of the email field.
 * @param onValueChange The callback function for when the value of the email field changes.
 * @param validator The form validator used to validate the email field value. Defaults to [FormValidator.EMAIL].
 * @param modifier The modifier for styling the email field. Defaults to Modifier.
 * @param label The label for the email field. Defaults to null.
 * @param keyboardOptions The keyboard options for the email field. Defaults to KeyboardOptions(keyboardType = KeyboardType.Email).
 * @param keyboardActions The keyboard actions for the email field. Defaults to KeyboardActions.Default.
 * @param leadingIcon The leading icon for the email field. Defaults to an email icon.
 * @param singleLine Whether the email field should be a single line or multiline. Defaults to true.
 * @param enabled Whether the email field should be enabled for user interaction. Defaults to true.
 * @param interactionSource The interaction source for the email field. Defaults to MutableInteractionSource.
 * @param textStyle The text style for the email field. Defaults to LocalTextStyle.current.
 * @param shape The shape of the email field. Defaults to TextFieldDefaults.shape.
 * @param colors The colors for the email field. Defaults to TextFieldDefaults.colors().
 * @param supportingText A composable function to display supporting text based on the validity of the email field value. Defaults to displaying "Please enter a valid email address" if the value is not a valid email.
 * @param trailingIcon The trailing icon for the email field. Defaults to null.
 * @param placeholder The placeholder for the email field. Defaults to null.
 * @param formKey The key to store the validity of the email field in the AuthState. Defaults to "EMAIL".
 * @param mandatory Whether the form field is mandatory or not. If false, will not affect the [AuthState.validForm] value. You can also make this value dynamic and only make the field mandatory, if e.g. the [value] is not empty. Default is true.
 */
@SupabaseExperimental
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
    mandatory: Boolean = true,
) {
    FormComponent(formKey, mandatory) {
        val isValidEmail = remember(value) { validator.validate(value) }
        LaunchedEffect(isValidEmail) {
            it.value = isValidEmail
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
            supportingText = { supportingText?.invoke(isValidEmail || value.isEmpty()) },
            trailingIcon = trailingIcon,
            placeholder = placeholder,
            enabled = enabled,
        )
    }
}

/**
 * A custom email input field with validation and pre-defined styling.
 *
 * @param value The current value of the email field.
 * @param onValueChange The callback function for when the value of the email field changes.
 * @param validator The form validator used to validate the email field value. Defaults to [FormValidator.EMAIL].
 * @param modifier The modifier for styling the email field. Defaults to Modifier.
 * @param label The label for the email field. Defaults to null.
 * @param keyboardOptions The keyboard options for the email field. Defaults to KeyboardOptions(keyboardType = KeyboardType.Email).
 * @param keyboardActions The keyboard actions for the email field. Defaults to KeyboardActions.Default.
 * @param leadingIcon The leading icon for the email field. Defaults to an email icon.
 * @param singleLine Whether the email field should be a single line or multiline. Defaults to true.
 * @param enabled Whether the email field should be enabled for user interaction. Defaults to true.
 * @param interactionSource The interaction source for the email field. Defaults to MutableInteractionSource.
 * @param textStyle The text style for the email field. Defaults to LocalTextStyle.current.
 * @param shape The shape of the email field. Defaults to TextFieldDefaults.shape.
 * @param colors The colors for the email field. Defaults to TextFieldDefaults.colors().
 * @param supportingText A composable function to display supporting text based on the validity of the email field value. Defaults to displaying "Please enter a valid email address" if the value is not a valid email.
 * @param trailingIcon The trailing icon for the email field. Defaults to null.
 * @param placeholder The placeholder for the email field. Defaults to null.
 * @param formKey The key to store the validity of the email field in the AuthState. Defaults to "EMAIL".
 * @param mandatory Whether the form field is mandatory or not. If false, will not affect the [AuthState.validForm] value. You can also make this value dynamic and only make the field mandatory, if e.g. the [value] is not empty. Default is true.
 */
@SupabaseExperimental
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
    mandatory: Boolean = true,
) {
    FormComponent(formKey, mandatory) {
        val isValidEmail = remember(value) { validator.validate(value.text) }
        LaunchedEffect(isValidEmail) {
            it.value = isValidEmail
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
            isError = !isValidEmail && value.text.isNotEmpty(),
            interactionSource = interactionSource,
            textStyle = textStyle,
            shape = shape,
            colors = colors,
            supportingText = { supportingText?.invoke(isValidEmail || value.text.isEmpty()) },
            trailingIcon = trailingIcon,
            placeholder = placeholder,
            enabled = enabled,
        )
    }
}
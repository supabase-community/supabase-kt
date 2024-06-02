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
import io.github.jan.supabase.compose.auth.ui.FormComponent
import io.github.jan.supabase.compose.auth.ui.annotations.AuthUiExperimental
import io.github.jan.supabase.compose.auth.ui.rememberLockIcon
import io.github.jan.supabase.compose.auth.ui.rememberVisibilityIcon
import io.github.jan.supabase.compose.auth.ui.rememberVisibilityOffIcon

/**
 * A custom password input field with custom rules and pre-defined styling.
 *
 * @param value The current value of the password field.
 * @param onValueChange The callback function for when the value of the password field changes.
 * @param rules The rules for the password field.
 * @param modifier The modifier for styling the password field. Defaults to Modifier.
 * @param label The label for the password field. Defaults to null.
 * @param keyboardOptions The keyboard options for the password field. Defaults to KeyboardOptions(keyboardType = KeyboardType.Password).
 * @param keyboardActions The keyboard actions for the password field. Defaults to KeyboardActions.Default.
 * @param leadingIcon The leading icon for the password field. Defaults to an email icon.
 * @param singleLine Whether the password field should be a single line or multiline. Defaults to true.
 * @param enabled Whether the password field should be enabled for user interaction. Defaults to true.
 * @param isError Whether the password field should display an error state. Defaults to null (handled automatically).
 * @param interactionSource The interaction source for the password field. Defaults to MutableInteractionSource.
 * @param textStyle The text style for the password field. Defaults to LocalTextStyle.current.
 * @param shape The shape of the password field. Defaults to TextFieldDefaults.shape.
 * @param colors The colors for the password field. Defaults to TextFieldDefaults.colors().
 * @param supportingText A composable function to display supporting text based on the validity of the password field value. Defaults to displaying "Please enter a valid email address" if the value is not a valid email.
 * @param trailingIcon The trailing icon for the password field. Defaults to null.
 * @param placeholder The placeholder for the password field. Defaults to null.
 * @param formKey The key to store the validity of the password field in the AuthState. Defaults to "EMAIL".
 * @param mandatory Whether the form field is mandatory or not. If false, will not affect the [AuthState.validForm] value. You can also make this value dynamic and only make the field mandatory, if e.g. the [value] is not empty. Default is true.
 */
@AuthUiExperimental
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
    isError: Boolean? = null,
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
    formKey: String = "PASSWORD",
    mandatory: Boolean = true
) {
    FormComponent(formKey, mandatory) {
        val showPassword = remember { mutableStateOf(false) }
        val ruleResults = remember(value, rules) { rules.map { PasswordRuleResult(it.description, it.predicate(value)) } }
        LaunchedEffect(value) {
            it.value = ruleResults.all { it.isFulfilled }
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = label,
            visualTransformation = visualTransformation(showPassword.value),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            isError = isError ?: (ruleResults.firstUnfulfilled() != null && value.isNotEmpty()),
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
}

/**
 * A custom password input field with custom rules and pre-defined styling.
 *
 * @param value The current value of the password field.
 * @param onValueChange The callback function for when the value of the password field changes.
 * @param rules The rules for the password field.
 * @param modifier The modifier for styling the password field. Defaults to Modifier.
 * @param label The label for the password field. Defaults to null.
 * @param keyboardOptions The keyboard options for the password field. Defaults to KeyboardOptions(keyboardType = KeyboardType.Password).
 * @param keyboardActions The keyboard actions for the password field. Defaults to KeyboardActions.Default.
 * @param leadingIcon The leading icon for the password field. Defaults to an email icon.
 * @param singleLine Whether the password field should be a single line or multiline. Defaults to true.
 * @param enabled Whether the password field should be enabled for user interaction. Defaults to true.
 * @param isError Whether the password field should display an error state. Defaults to null (handled automatically).
 * @param interactionSource The interaction source for the password field. Defaults to MutableInteractionSource.
 * @param textStyle The text style for the password field. Defaults to LocalTextStyle.current.
 * @param shape The shape of the password field. Defaults to TextFieldDefaults.shape.
 * @param colors The colors for the password field. Defaults to TextFieldDefaults.colors().
 * @param supportingText A composable function to display supporting text based on the validity of the password field value. Defaults to displaying "Please enter a valid email address" if the value is not a valid email.
 * @param trailingIcon The trailing icon for the password field. Defaults to null.
 * @param placeholder The placeholder for the password field. Defaults to null.
 * @param formKey The key to store the validity of the password field in the AuthState. Defaults to "EMAIL".
 * @param mandatory Whether the form field is mandatory or not. If false, will not affect the [AuthState.validForm] value. You can also make this value dynamic and only make the field mandatory, if e.g. the [value] is not empty. Default is true.
 */
@AuthUiExperimental
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
    isError: Boolean? = null,
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
    formKey: String = "PASSWORD",
    mandatory: Boolean = true
) {
    FormComponent(formKey, mandatory) {
        val showPassword = remember { mutableStateOf(false) }
        val ruleResults = remember(value, rules) { rules.map { PasswordRuleResult(it.description, it.predicate(value.text)) } }
        LaunchedEffect(value) {
            it.value = ruleResults.all { it.isFulfilled }
        }
        TextField(
            value = value,
            onValueChange = onValueChange,
            label = label,
            visualTransformation = visualTransformation(showPassword.value),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            isError = isError ?: (ruleResults.firstUnfulfilled() != null && value.text.isNotEmpty()),
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
}

/**
 * A custom password input field with custom rules and pre-defined styling.
 *
 * @param value The current value of the password field.
 * @param onValueChange The callback function for when the value of the password field changes.
 * @param rules The rules for the password field.
 * @param modifier The modifier for styling the password field. Defaults to Modifier.
 * @param label The label for the password field. Defaults to null.
 * @param keyboardOptions The keyboard options for the password field. Defaults to KeyboardOptions(keyboardType = KeyboardType.Password).
 * @param keyboardActions The keyboard actions for the password field. Defaults to KeyboardActions.Default.
 * @param leadingIcon The leading icon for the password field. Defaults to an email icon.
 * @param singleLine Whether the password field should be a single line or multiline. Defaults to true.
 * @param enabled Whether the password field should be enabled for user interaction. Defaults to true.
 * @param isError Whether the password field should display an error state. Defaults to null (handled automatically).
 * @param interactionSource The interaction source for the password field. Defaults to MutableInteractionSource.
 * @param textStyle The text style for the password field. Defaults to LocalTextStyle.current.
 * @param shape The shape of the password field. Defaults to TextFieldDefaults.shape.
 * @param colors The colors for the password field. Defaults to TextFieldDefaults.colors().
 * @param supportingText A composable function to display supporting text based on the validity of the password field value. Defaults to displaying "Please enter a valid email address" if the value is not a valid email.
 * @param trailingIcon The trailing icon for the password field. Defaults to null.
 * @param placeholder The placeholder for the password field. Defaults to null.
 * @param formKey The key to store the validity of the password field in the AuthState. Defaults to "EMAIL".
 * @param mandatory Whether the form field is mandatory or not. If false, will not affect the [AuthState.validForm] value. You can also make this value dynamic and only make the field mandatory, if e.g. the [value] is not empty. Default is true.
 */
@AuthUiExperimental
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
    isError: Boolean? = null,
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
    formKey: String = "PASSWORD",
    mandatory: Boolean = true
) {
    FormComponent(formKey, mandatory) {
        val showPassword = remember { mutableStateOf(false) }
        val ruleResults = remember(value, rules) { rules.map { PasswordRuleResult(it.description, it.predicate(value)) } }
        LaunchedEffect(value) {
            it.value = ruleResults.all { it.isFulfilled }
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = label,
            visualTransformation = visualTransformation(showPassword.value),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            isError = isError ?: (ruleResults.firstUnfulfilled() != null && value.isNotEmpty()),
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
}

/**
 * A custom password input field with custom rules and pre-defined styling.
 *
 * @param value The current value of the password field.
 * @param onValueChange The callback function for when the value of the password field changes.
 * @param rules The rules for the password field.
 * @param modifier The modifier for styling the password field. Defaults to Modifier.
 * @param label The label for the password field. Defaults to null.
 * @param keyboardOptions The keyboard options for the password field. Defaults to KeyboardOptions(keyboardType = KeyboardType.Password).
 * @param keyboardActions The keyboard actions for the password field. Defaults to KeyboardActions.Default.
 * @param leadingIcon The leading icon for the password field. Defaults to an email icon.
 * @param singleLine Whether the password field should be a single line or multiline. Defaults to true.
 * @param enabled Whether the password field should be enabled for user interaction. Defaults to true.
 * @param isError Whether the password field should display an error state. Defaults to null (handled automatically).
 * @param interactionSource The interaction source for the password field. Defaults to MutableInteractionSource.
 * @param textStyle The text style for the password field. Defaults to LocalTextStyle.current.
 * @param shape The shape of the password field. Defaults to TextFieldDefaults.shape.
 * @param colors The colors for the password field. Defaults to TextFieldDefaults.colors().
 * @param supportingText A composable function to display supporting text based on the validity of the password field value. Defaults to displaying "Please enter a valid email address" if the value is not a valid email.
 * @param trailingIcon The trailing icon for the password field. Defaults to null.
 * @param placeholder The placeholder for the password field. Defaults to null.
 * @param formKey The key to store the validity of the password field in the AuthState. Defaults to "EMAIL".
 * @param mandatory Whether the form field is mandatory or not. If false, will not affect the [AuthState.validForm] value. You can also make this value dynamic and only make the field mandatory, if e.g. the [value] is not empty. Default is true.
 */
@AuthUiExperimental
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
    isError: Boolean? = null,
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
    formKey: String = "PASSWORD",
    mandatory: Boolean = true
) {
    FormComponent(formKey, mandatory) {
        val showPassword = remember { mutableStateOf(false) }
        val ruleResults = remember(value, rules) { rules.map { PasswordRuleResult(it.description, it.predicate(value.text)) } }
        LaunchedEffect(value) {
            it.value = ruleResults.all { it.isFulfilled }
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = label,
            visualTransformation = visualTransformation(showPassword.value),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            isError = isError ?: (ruleResults.firstUnfulfilled() != null && value.text.isNotEmpty()),
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
}

private fun List<PasswordRuleResult>.firstUnfulfilled(): PasswordRuleResult? = firstOrNull { !it.isFulfilled }
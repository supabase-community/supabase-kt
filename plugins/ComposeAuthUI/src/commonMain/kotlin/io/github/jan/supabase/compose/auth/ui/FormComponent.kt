package io.github.jan.supabase.compose.auth.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import io.github.jan.supabase.compose.auth.ui.annotations.AuthUiExperimental

/**
 * A component that represents a form field.
 *
 * @param key A unique identifier for the form field.
 * @param mandatory Whether the form field is mandatory or not. If false, will not affect the [AuthState.validForm] value. Default is true.
 * @param content The composable function that defines the content of the form field and receives a mutable state object as a parameter.
 */
@AuthUiExperimental
@Composable
fun FormComponent(key: String, mandatory: Boolean = true, content: @Composable (valid: MutableState<Boolean>) -> Unit) {
    val state = LocalAuthState.current
    val formState = rememberSaveable { mutableStateOf(state[key] ?: false) }
    LaunchedEffect(formState.value, mandatory) {
        if(mandatory) {
            state[key] = formState.value
        } else {
            if(formState.value) {
                state[key] = formState.value
            } else {
                state.remove(key)
            }
        }
    }
    content(formState)
}
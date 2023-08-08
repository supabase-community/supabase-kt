package io.github.jan.supabase.compose.auth.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

class AuthState {

    //TODO: Make this more extensible
    internal var validPassword by mutableStateOf(false)

    internal var validEmail by mutableStateOf(false)

    val validForm: Boolean
        get() = validEmail && validPassword

}

val LocalAuthState = compositionLocalOf {
    AuthState() //possibly to throw an error here
}

@Composable
fun AuthForm(
    state: AuthState = remember { AuthState() },
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalAuthState provides state) {
        content()
    }
}
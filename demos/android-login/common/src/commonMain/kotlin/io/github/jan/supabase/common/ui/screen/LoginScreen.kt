package io.github.jan.supabase.common.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.compose.auth.ui.AuthForm
import io.github.jan.supabase.compose.auth.ui.LocalAuthState
import io.github.jan.supabase.compose.auth.ui.ProviderIcon
import io.github.jan.supabase.compose.auth.ui.email.OutlinedEmailField
import io.github.jan.supabase.compose.auth.ui.password.OutlinedPasswordField
import io.github.jan.supabase.gotrue.providers.Google

sealed interface LoginType {
    data class Login(val email: String, val password: String) : LoginType
    data class SignUp(val email: String, val password: String) : LoginType
    data object GoogleNative : LoginType
    data object SpotifyInApp: LoginType
}

@OptIn(ExperimentalMaterial3Api::class, SupabaseExperimental::class)
@Composable
fun LoginScreen(onLogin: (LoginType) -> Unit) {
    var signUp by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val passwordFocus = remember { FocusRequester() }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AuthForm {
            OutlinedEmailField(
                value = email,
                onValueChange = { email = it },
                label = { Text("E-Mail") },
            )
            OutlinedPasswordField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
            )

            Button(
                onClick = {
                    onLogin(
                        if (signUp) LoginType.SignUp(email, password) else LoginType.Login(
                            email,
                            password
                        )
                    )
                },
                modifier = Modifier.padding(top = 10.dp),
                enabled = LocalAuthState.current.validForm
            ) {
                Text(if (signUp) "Register" else "Login")
            }
        }
        OutlinedButton(
            onClick = { onLogin(LoginType.GoogleNative) }
        ) {
            ProviderIcon(Google, null)
            Spacer(Modifier.width(4.dp))
            Text(if (signUp) "Sign Up with Google OneTap" else "Login with Google OneTap")
        }
        OutlinedButton(
            onClick = { onLogin(LoginType.SpotifyInApp) }
        ) {
            Text(if (signUp) "Sign Up with Spotify using WebView" else "Login with Spotify using WebView")
        }

    }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        TextButton(onClick = { signUp = !signUp }) {
            Text(if (signUp) "Already have an account? Login" else "Not registered? Register")
        }
    }
}
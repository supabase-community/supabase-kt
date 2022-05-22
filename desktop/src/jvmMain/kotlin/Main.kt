// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.soywiz.klock.seconds
import io.github.jan.supacompose.auth.Auth
import io.github.jan.supacompose.auth.auth
import io.github.jan.supacompose.auth.compose.ProviderButton
import io.github.jan.supacompose.auth.providers.Discord
import io.github.jan.supacompose.auth.providers.Email
import io.github.jan.supacompose.auth.providers.OAuthFail
import io.github.jan.supacompose.createSupabaseClient
import kotlinx.coroutines.launch

suspend fun main() {
    val client = createSupabaseClient {
        supabaseUrl = System.getenv("SUPABASE_URL")
        supabaseKey = System.getenv("SUPABASE_KEY")

        install(Auth)
    }
    application {
        Window(::exitApplication) {
            val session by client.auth.currentSession.collectAsState()
            val scope = rememberCoroutineScope()
                if(session != null) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text("Logged in as ${session?.user?.email}")
                    }
                } else {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        var email by remember { mutableStateOf("") }
                        var password by remember { mutableStateOf("") }
                        Column {
                            TextField(email, { email = it }, placeholder = { Text("Email") })
                            TextField(
                                password,
                                { password = it },
                                placeholder = { Text("Password") },
                                visualTransformation = PasswordVisualTransformation()
                            )
                            Button(onClick = {
                                scope.launch {
                                    client.auth.signUpWith(Email) {
                                        this.email = email
                                        this.password = password
                                    }
                                }
                            }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                                Text("Login")
                            }
                            ProviderButton(
                                icon = {
                                    Icon(painterResource("discord_icon.svg"), "", modifier = Modifier.size(25.dp))
                                },
                                text = {
                                    Text("Log in with Discord")
                                },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                scope.launch {
                                    client.auth.loginWith(Discord, onFail = {
                                        when (it) {
                                            is OAuthFail.Timeout -> {
                                                println("Timeout")
                                            }
                                            is OAuthFail.Error -> {
                                                //log error
                                            }
                                        }
                                    }) {
                                        timeout = 50.seconds
                                        htmlTitle = "SupaCompose"
                                        htmlText = "Logged in. You may continue in the app."
                                    }
                                }
                            }
                        }
                    }

            }
        }
    }

}

/*fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}*/

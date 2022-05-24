package io.github.jan.supacompose.android

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import io.github.jan.supacompose.auth.Auth
import io.github.jan.supacompose.auth.DeepLinks
import io.github.jan.supacompose.auth.auth
import io.github.jan.supacompose.auth.handleDeepLinks
import io.github.jan.supacompose.auth.providers.Email
import io.github.jan.supacompose.createSupabaseClient
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    val supabaseClient = createSupabaseClient {

        supabaseUrl = "your supabase url"
        supabaseKey = "your supabase key"

        install(Auth)
        install(DeepLinks) {
            scheme = "supacompose"
            host = "login"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleDeepLinks(supabaseClient)
        setContent {
            MaterialTheme {
                val session by supabaseClient.auth.currentSession.collectAsState()
                println(session)
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
                                    supabaseClient.auth.loginWith(Email) {
                                        this.email = email
                                        this.password = password
                                    }
                                }
                            }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                                Text("Login")
                            }
                            //
                        }
                        }
                    }
            }
        }
    }

}
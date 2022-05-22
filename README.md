# SupaCompose
A framework for building android & desktop apps with Supabase

https://user-images.githubusercontent.com/26686035/169712245-e090e33b-8472-49c8-a512-947d5ed889d5.mp4

# Features

SupaCompose currently supports:

#### Authentication

|         | Login                           | Signup                          | Verifying (Signup, Password Reset, Invite)  | Logout | Otp     |
|---------|---------------------------------|---------------------------------|---------------------------------------------|--------|---------|
| Desktop | phone, password, oauth2         | phone, password, oauth2         | only with token                             | yes    | no      |
| Android | phone, password, oauth2 planned | phone, password, oauth2 planned | only with token, url authentication planned | yes    | planned |

# Example code
<details><summary>Authentication</summary>
<p>

```kotlin
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
```

</p>
</details>

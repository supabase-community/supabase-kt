# Supabase-kt Compose Auth

Extends gotrue-kt with auth composable

Newest version: [![Maven Central](https://img.shields.io/maven-central/v/io.github.jan-tennert.supabase/supabase-kt)](https://search.maven.org/search?q=g%3Aio.github.jan-tennert.supabase)

# Installation

```kotlin
dependencies {
    implementation("io.github.jan-tennert.supabase:compose-auth:VERSION")
}
```

Install plugin in main SupabaseClient. See the [documentation](https://supabase.com/docs/reference/kotlin/initializing) for more information
```kotlin
val client = createSupabaseClient(
    supabaseUrl = "https://id.supabase.co",
    supabaseKey = "apikey"
) {
    //...
    install(GoTrue){
        //your config
    }
    install(ComposeAuth) {
        googleNativeLogin(serverClientId = "google-client-id")
        appleNativeLogin()
    }
}
```

# Usage

The composable can be accessed trough `composeAuth` property from `client`

```kotlin
val action = client.composeAuth.rememberLoginWithGoogle(
    onResult = { result ->
        when (result) {
            is NativeSignInResult.Success -> {}
            is NativeSignInResult.ClosedByUser -> {}
            is NativeSignInResult.Error -> {}
            is NativeSignInResult.NetworkError -> {}
        } 
    },
    fallback = {
    // optional: only add fallback if you like to use custom fallback
    }
                            
Button(onClick = { action.startFlow() }) { Text(text = "Google Login") })
```

# Support

Currently, Compose Auth only supports native login for
Android with Google and iOS with Apple, other variations such as JS rely on fallback which
by default is GoTrue-kt OAuth flow.

To learn how you can use this plugin in your compose project, visit [Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform/#readme)
# Supabase-kt Compose Auth

Extends GoTrue-kt with auth composable

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
        googleLoginConfig = googleNativeLogin(serverClientId = "google-client-id")
        appleLoginConfig = appleNativeLogin()
    }
}
```

# Usage

The composable can be accessed trough `composeAuth` property from `client`

```kotlin
val action = client.composeAuth.rememberLoginWithGoogle(onResult = {})

//...

Button(onClick = { action.startFlow() }) { Text(text = "Google Login") }
```

To learn how you can use this plugin, visit [Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform/#readme)
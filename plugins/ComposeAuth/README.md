# Supabase-kt Compose Auth

Extends gotrue-kt with auth composables for Compose Multiplatform

Supported targets:

| Target | **JVM** | **Android** | **JS** | **iOS** | **tvOS** | **watchOS** | **macOS** | **Windows** | **Linux** |
|--------|---------|-------------|--------|---------|----------|-------------|-----------|-------------|-----------|
| Status | ☑️      | ✅           | 	☑️    | ✅      | 	❌       | 	❌          | 	❌        | ❌           | ❌         |

> Note: iOS support is experimental and needs feedback

<details>

<summary>In-depth Kotlin targets</summary>

**iOS:** iosArm64, iosSimulatorArm64, iosX64

**JS**: Browser, NodeJS

**tvOS**: tvosArm64, tvosX64, tvosSimulatorArm64

**watchOS**: watchosArm64, watchosX64, watchosSimulatorArm64

**MacOS**: macosX64, macosArm64

**Windows**: mingwX64

**Linux**: linuxX64

</details>

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
    install(GoTrue) {
        //your config
    }
    install(ComposeAuth) {
        googleNativeLogin(serverClientId = "google-client-id")
        appleNativeLogin()
    }
}
```

# Support

Currently, Compose Auth only supports native login for
Android with Google and iOS with Apple, other variations such as JS and JVM rely on fallback which
by default is GoTrue-kt OAuth flow.

To learn how you can use this plugin in your compose project, visit [Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform/#readme)

# Usage

The composable can be accessed trough `composeAuth` property from `client`

```kotlin
val action = client.composeAuth.rememberLoginWithGoogle(
    onResult = { result -> //optional error handling
        when (result) {
            is NativeSignInResult.Success -> {}
            is NativeSignInResult.ClosedByUser -> {}
            is NativeSignInResult.Error -> {}
            is NativeSignInResult.NetworkError -> {}
        } 
    },
    fallback = { // optional: add custom error handling, not required by default
    
    }
)
                            
Button(
    onClick = { action.startFlow() }
) { 
    Text("Google Login") 
}
```

# Native Google Auth on Android

Here is a guide on how to use Native Google Auth on Android

1. Create a project in your [Google Cloud Developer Console](console.cloud.google.com/)
2. Create OAuth credentials for a Web application, and use your Supabase callback url as redirect url. (**https://ID.supabase.co/auth/v1/callback**)
3. Put in the Web OAuth in your Supabase Auth Settings for Google in the Dashboard
4. Create OAuth credentials for an Android app, and put in your package name and SHA-1 certificate (which you can get by using `gradlew signingReport`)
5. Put the Android OAuth client id to the authorized client ids in the Supabase Dashboard
6. Use the **Web** OAuth client id in the Compose Auth plugin

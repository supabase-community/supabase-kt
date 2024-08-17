# Supabase-kt

The main Supabase-kt library. It provides a plugin system to extend the client with additional features and provides basic functionality to interact with the Supabase API.

Supported targets:

| Target | **JVM** | **Android** | **JS** | **iOS** | **tvOS** | **watchOS** | **macOS** | **Windows** | **Linux** |
| ------ | ------- | ----------- | ------ | ------- | -------- | ----------- | --------- | ----------- | --------- |
|        | ✅      | ✅          | ✅     | ✅      | ✅       | ✅          | ✅        | ✅          | ✅        |

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

# Installation

*You don't necessarily need to install this library directly. It is a dependency of the other Supabase-kt libraries.*

Newest version: [![](https://img.shields.io/github/release/supabase-community/supabase-kt?label=)](https://github.com/supabase-community/supabase-kt/releases)

```kotlin
dependencies {
    implementation("io.github.jan-tennert.supabase:supabase-kt:VERSION")
}
```

# Usage

```kotlin
//Create a Supabase client
val supabase = createSupabaseClient(
    supabaseUrl = "https://id.supabase.co",
    supabaseKey = "apikey"
) {
    //Change default settings
    defaultSerializer = MyCustomSerializer()
    defaultLogLevel = LogLevel.DEBUG
    
    //Install a plugin
    install(Auth) //from gotrue-kt

}

//Access a plugin via the client
val auth = supabase.auth
```
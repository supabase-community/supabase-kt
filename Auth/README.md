# Supabase-kt GoTrue

Extends Supabase-kt with a multiplatform GoTrue client.

Supported targets:

| Target | **JVM** | **Android** | **JS** | **iOS** | **tvOS** | **watchOS** | **macOS** | **Windows** | **Linux** |
| ------ | ------- | ----------- | ------ | ------- | -------- | ----------- | --------- | ----------- | --------- |
|        | ✅      | ✅          | ✅     | ✅      | ☑️       | ☑️          | ✅        | ☑️          | ☑️        |

> Native support is experimental and needs feedback
>
> ☑️ = No built-in OAuth support. Linux has no support for persistent session storage.

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

Newest version: [![](https://img.shields.io/github/release/supabase-community/supabase-kt?label=)](https://github.com/supabase-community/supabase-kt/releases)

```kotlin
dependencies {
    implementation("io.github.jan-tennert.supabase:auth-kt:VERSION")
}
```

Install the plugin in your SupabaseClient. See the [documentation](https://supabase.com/docs/reference/kotlin/initializing) for more information

```kotlin
val supabase = createSupabaseClient(
    supabaseUrl = "https://id.supabase.co",
    supabaseKey = "apikey"
) {

    //...

    install(Auth) {
        // settings
    }

}
```

# Usage

See [GoTrue documentation](https://supabase.com/docs/reference/kotlin/auth-signup) for usage

# Supabase-kt GoTrue

Extends Supabase-kt with a multiplatform GoTrue client.

Supported targets:

| Target | **JVM** | **Android** | **JS** | **iOS** | **tvOS** | **watchOS** | **macOS** | **Windows** | **Linux** |
|--------|---------|-------------|--------|---------|----------|-------------|-----------|-------------|-----------|
| Status | ✅       | ✅           | ✅      | ✅       | 	☑️      | 	 ☑️         | 	 ✅       | ☑️           | ☑️         |

> Note: Native support is experimental and needs feedback

☑️ = No built-in OAuth support. Linux has no support for persistent session storage.

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
    implementation("io.github.jan-tennert.supabase:gotrue-kt:VERSION")
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
        // settings
    }
    
}
```

or create standalone module
```kotlin
val gotrue = standaloneSupabaseModule(GoTrue, url = "https://your.gotrue.url.com", apiKey = "your-api-key")
```

# Usage

See [GoTrue documentation](https://supabase.com/docs/reference/kotlin/auth-signup) for usage
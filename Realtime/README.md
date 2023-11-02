# Supabase-kt Realtime

Extends Supabase-kt with a multiplatform Realtime client.

Supported targets:

| Target | **JVM** | **Android** | **JS** | **iOS** | **tvOS** | **watchOS** | **macOS** | **Windows** | **Linux** |
|--------|---------|-------------|--------|---------|----------|-------------|-----------|-------------|-----------|
| Status | ✅       | ✅           | ✅      | ✅       | 	 ✅      | 	 ✅         | 	 ✅       | ✅           | ✅         |

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
    implementation("io.github.jan-tennert.supabase:realtime-kt:VERSION")
}
```

Install plugin in main SupabaseClient. See the [documentation](https://supabase.com/docs/reference/kotlin/initializing) for more information
```kotlin
val client = createSupabaseClient(
    supabaseUrl = "https://id.supabase.co",
    supabaseKey = "apikey"
) {
    
    //...
    
    install(Realtime) {
        // settings
    }
    
}
```

or create standalone module
```kotlin
val realtime = standaloneSupabaseModule(Realtime, url = "wss://your.realtime.url.com", apiKey = "your-api-key")
```

# Usage

See [Realtime documentation](https://supabase.com/docs/reference/kotlin/subscribe) for usage
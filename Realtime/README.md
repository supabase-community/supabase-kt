# Supabase-kt Realtime

Extends Supabase-kt with a multiplatform Realtime client.

Newest version: [![Maven Central](https://img.shields.io/maven-central/v/io.github.jan-tennert.supabase/supabase-kt)](https://search.maven.org/search?q=g%3Aio.github.jan-tennert.supabase)

# Installation

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
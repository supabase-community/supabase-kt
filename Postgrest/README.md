# Supabase-kt Postgrest

Extends Supabase-kt with a multiplatform Postgrest client.

Newest version: [![Maven Central](https://img.shields.io/maven-central/v/io.github.jan-tennert.supabase/supabase-kt)](https://search.maven.org/search?q=g%3Aio.github.jan-tennert.supabase)

# Installation

```kotlin
dependencies {
    implementation("io.github.jan-tennert.supabase:postgrest-kt:VERSION")
}
```

Install plugin in main SupabaseClient. See the [documentation](https://supabase.com/docs/reference/kotlin/initializing) for more information
```kotlin
val client = createSupabaseClient(
    supabaseUrl = "https://id.supabase.co",
    supabaseKey = "apikey"
) {
    
    //...
    
    install(Postgrest) {
        // settings
    }
    
}
```

or create standalone module
```kotlin
val postgrest = standaloneSupabaseModule(Postgrest, url = "https://your.postgrest.url.com", apiKey = "your-api-key")
```

# Usage

See [Postgrest documentation](https://supabase.com/docs/reference/kotlin/select) for usage
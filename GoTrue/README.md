# Supabase-kt GoTrue

Extends Supabase-kt with a multiplatform GoTrue client.

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
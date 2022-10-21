# Supabase-kt Postgrest

Extends Supabase-kt with a multiplatform Postgrest client.

Newest version: [![Maven Central](https://img.shields.io/maven-central/v/io.github.jan-tennert.supabase/supabase-kt)](https://search.maven.org/search?q=g%3Aio.github.jan-tennert.supabase)

# Installation

```kotlin
dependencies {
    implementation("io.github.jan-tennert.supabase:postgrest-kt:VERSION")
}
```

Install plugin in main supabase client. See [Getting started](https://github.com/supabase-community/supabase-kt/wiki/Getting-Started) for more information
```kotlin
val client = createSupabaseClient {
    
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

See [Postgrest docs](https://github.com/supabase-community/supabase-kt/wiki/Postgrest#usage) for usage
# Supabase-kt Functions

Extends Supabase-kt with a multiplatform Functions client.

Newest version: [![Maven Central](https://img.shields.io/maven-central/v/io.github.jan-tennert.supabase/supabase-kt)](https://search.maven.org/search?q=g%3Aio.github.jan-tennert.supabase)

# Installation

```kotlin
dependencies {
    implementation("io.github.jan-tennert.supabase:functions-kt:VERSION")
}
```

Install plugin in main supabase client. See [Getting started](https://github.com/supabase-community/supabase-kt/wiki/Getting-Started) for more information
```kotlin
val client = createSupabaseClient {
    
    //...
    
    install(Functions) {
        // settings
    }
    
}
```

or create standalone module
```kotlin
val storage = standaloneSupabaseModule(Functions, url = "https://your.storage.url.com", apiKey = "your-api-key")
```

# Usage

See [Functions docs](https://github.com/supabase-community/supabase-kt/wiki/Functions#usage) for usage

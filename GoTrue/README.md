# Supabase-kt GoTrue

Extends Supabase-kt with a multiplatform GoTrue client.

Newest version: [![Maven Central](https://img.shields.io/maven-central/v/io.github.jan-tennert.supabase/supabase-kt)](https://search.maven.org/search?q=g%3Aio.github.jan-tennert.supabase)

# Installation

```kotlin
dependencies {
    implementation("io.github.jan-tennert.supabase:gotrue-kt:VERSION")
}
```

Install plugin in main supabase client. See [Getting started](https://github.com/supabase-community/supabase-kt/wiki/Getting-Started) for more information
```kotlin
val client = createSupabaseClient {
    
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

See [GoTrue docs](https://github.com/supabase-community/supabase-kt/wiki/GoTrue#usage) for usage
# Supabase-kt Realtime

Extends Supabase-kt with a multiplatform Realtime client.

Newest version: [![Maven Central](https://img.shields.io/maven-central/v/io.github.jan-tennert.supabase/supabase-kt)](https://search.maven.org/search?q=g%3Aio.github.jan-tennert.supabase)

# Installation

```kotlin
dependencies {
    implementation("io.github.jan-tennert.supabase:realtime-kt:VERSION")
}
```

Install plugin in main supabase client. See [Getting started](https://github.com/supabase-community/supabase-kt/wiki/Getting-Started) for more information
```kotlin
val client = createSupabaseClient {
    
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

See [Realtime docs](https://github.com/supabase-community/supabase-kt/wiki/Realtime#usage) for usage
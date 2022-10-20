# Supabase-kt Storage

Extends Supabase-kt with a multiplatform Storage client.

Newest version: [![Maven Central](https://img.shields.io/maven-central/v/io.github.jan-tennert.supabase/supabase-kt)](https://search.maven.org/search?q=g%3Aio.github.jan-tennert.supabase)

# Installation

```kotlin
dependencies {
    implementation("io.github.jan-tennert.supabase:storage-kt:VERSION")
}
```

Install plugin in main supabase client. See [Getting started](https://github.com/supabase-community/supabase-kt/wiki/Getting-Started) for more information
```kotlin
val client = createSupabaseClient {
    
    //...
    
    install(Storage) {
        // settings
    }
    
}
```

# Usage

See [Storage docs](https://github.com/supabase-community/supabase-kt/wiki/Storage#usage) for usage
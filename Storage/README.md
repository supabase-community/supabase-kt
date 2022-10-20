# Supabase-kt Storage

Extends Supabase-kt with a multiplatform Storage client.

Newest version: [![Maven Central](https://img.shields.io/maven-central/v/io.github.jan-tennert.supabase/supabase-kt)](https://search.maven.org/search?q=g%3Aio.github.jan-tennert.supabase)

# Installation

```kotlin
dependencies {
    implementation("io.github.jan-tennert.supabase:storage-kt:VERSION")
}
```

Install plugin in main supabase client. See [supabase-kt](https://github.com/supabase-community/supabase-kt) for more information
```kotlin
val client = createSupabaseClient {
    
    //...
    
    install(Storage) {
        // settings
    }
    
}
```

# Usage

See [Storage docs](https://github.com/supabase-community/supabase-kt/wiki/Storage#features) for usage
# Supabase-kt GoTrue

Extends Supabase-kt with a multiplatform GoTrue client.

Newest version: [![Maven Central](https://img.shields.io/maven-central/v/io.github.jan-tennert.supabase/supabase-kt)](https://search.maven.org/search?q=g%3Aio.github.jan-tennert.supabase)

# Installation

```kotlin
dependencies {
    implementation("io.github.jan-tennert.supabase:gotrue-kt:VERSION")
}
```

Install plugin in main supabase client. See [supabase-kt](https://github.com/supabase-community/supabase-kt) for more information
```kotlin
val client = createSupabaseClient {
    
    //...
    
    install(GoTrue) {
        // settings
    }
    
}
```

# Usage

See [GoTrue docs](https://github.com/supabase-community/supabase-kt/wiki/GoTrue#features) for usage
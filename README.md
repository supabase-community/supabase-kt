# supabase-kt

A Kotlin Multiplatform Client for Supabase.
Supported targets:
- JVM
- Android
- JS (Browser)
- _iOS (planned)_

Newest version: [![Maven Central](https://img.shields.io/maven-central/v/io.github.jan-tennert.supabase/supabase-kt)](https://search.maven.org/search?q=g%3Aio.github.jan-tennert.supabase)

# Installation

```kotlin
dependencies {
    implementation("io.github.jan-tennert.supabase:[module e.g. functions-kt or gotrue-kt]:VERSION")

    //add ktor client engine (if you don't already have one, see https://ktor.io/docs/http-client-engines.html for all engines)
    //e.g. the CIO engine
    implementation("io.ktor:ktor-client-cio:KTOR_VERSION")
}
```

# [Getting started](https://github.com/supabase-community/supabase-kt/wiki/Getting-Started)

# Features

#### [Authentication (GoTrue)](/GoTrue)

#### [Database/Postgrest](/Postgrest)

#### [Storage](/Storage)

#### [Realtime](/Realtime)

#### [Functions (Edge Functions)](/Functions)

# Credits

- Postgres Syntax inspired by https://github.com/supabase-community/postgrest-kt
- Plugin system inspired by ktor

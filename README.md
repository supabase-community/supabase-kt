# supabase-kt

A Kotlin Multiplatform Client for Supabase.
Supported targets:
- JVM
- Android
- JS (Browser) 
- IOS (experimental)

*Note: WASM planned see [issue](https://github.com/supabase-community/supabase-kt/issues/86)*

[![](https://img.shields.io/github/release/supabase-community/supabase-kt?label=stable)](https://github.com/supabase-community/supabase-kt/releases) [![](https://img.shields.io/maven-central/v/io.github.jan-tennert.supabase/supabase-kt?label=experimental)](https://central.sonatype.com/search?q=io.github.jan.supabase&smo=true)

### Links

[Documentation](https://supabase.com/docs/reference/kotlin/introduction)

[Dokka documentation for the latest version](https://supabase-community.github.io/supabase-kt/)

[Troubleshooting](https://github.com/supabase-community/supabase-kt/wiki/Troubleshooting)

# Installation

**Available modules**: `gotrue-kt`, `postgrest-kt`, `functions-kt`, `storage-kt`, `realtime-kt`, `apollo-graphql`

```kotlin
dependencies {
    implementation("io.github.jan-tennert.supabase:[module]:VERSION")

    //add ktor client engine (if you don't already have one, see https://ktor.io/docs/http-client-engines.html for all engines)
    //e.g. the CIO engine
    implementation("io.ktor:ktor-client-cio:KTOR_VERSION")
}
```

If you use multiple modules, you can use the bom dependency to get the correct versions for all modules:

```kotlin
implementation(platform("io.github.jan-tennert.supabase:bom:VERSION"))
implementation("io.github.jan-tennert.supabase:[module]")
```

# Main Modules

#### [Authentication (GoTrue)](/GoTrue)

#### [Database/Postgrest](/Postgrest)

#### [Storage](/Storage)

#### [Realtime](/Realtime)

#### [Functions (Edge Functions)](/Functions)

### Plugins

#### [Apollo GraphQL integration](/plugins/ApolloGraphQL)

# Demos

- [Chat Demo (Desktop/Android/Browser)](https://github.com/supabase-community/supabase-kt/tree/master/demos/chat-demo-mpp)
- [File Upload Demo (Desktop/Android)](https://github.com/supabase-community/supabase-kt/tree/master/demos/file-upload)
- [Android Native Google login & in-app OAuth (Android)](https://github.com/supabase-community/supabase-kt/tree/master/demos/android-login)
- [Multi-Factor Authentication (Desktop/Android/Browser)](https://github.com/supabase-community/supabase-kt/tree/master/demos/multi-factor-authentication)
- [Multiplatform Deep Linking (Desktop/Android)](https://github.com/supabase-community/supabase-kt/tree/master/demos/multiplatform-deeplinks)
- [Groceries Store App (Android)](https://github.com/hieuwu/android-groceries-store)

# Videos

- [Getting started with Supabase on Android](https://www.youtube.com/watch?v=SGr73sWMX6w) (by [Eric Ampire](https://www.youtube.com/@eric-ampire))

# Contribution

### How to contribute

1. Fork the repository
2. Create a branch 
3. Make your changes
4. Submit a pull request with your new branch and add one of the following tags: `fix` or `enhancement`. You might also add one or more label when you are modifying a specific module: `gotrue`, `realtime`, `functions`, `graphql`, `storage`, `postgrest`, `core`

# Credits

- Postgres Syntax inspired by https://github.com/supabase-community/postgrest-kt
- Plugin system inspired by ktor

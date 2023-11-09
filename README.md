# supabase-kt

A Kotlin Multiplatform Client for Supabase.

For information about supported Kotlin targets, see the corresponding module README.

*Note: WASM support planned see [issue](https://github.com/supabase-community/supabase-kt/issues/86)*

[![](https://img.shields.io/github/release/supabase-community/supabase-kt?label=stable)](https://github.com/supabase-community/supabase-kt/releases) [![](https://badgen.net/github/release/supabase-community/supabase-kt?label=prerelease)](https://central.sonatype.com/search?q=io.github.jan.supabase&smo=true)

### Links

[Documentation](https://supabase.com/docs/reference/kotlin/introduction)

[Quickstart](https://supabase.com/docs/guides/getting-started/quickstarts/kotlin)

[Tutorial: Build a Product Management Android App with Jetpack Compose](https://supabase.com/docs/guides/getting-started/tutorials/with-kotlin)

[Dokka documentation for the latest version](https://supabase-community.github.io/supabase-kt/)

[Troubleshooting](https://github.com/supabase-community/supabase-kt/wiki/Troubleshooting)

# Installation

**Available modules**: `gotrue-kt`, `postgrest-kt`, `functions-kt`, `storage-kt`, `realtime-kt`, `apollo-graphql`, `compose-auth`, `compose-auth-ui`

```kotlin
dependencies {
    implementation("io.github.jan-tennert.supabase:[module]:VERSION")

    //add ktor client engine (if you don't already have one, see https://ktor.io/docs/http-client-engines.html for all engines)
    //e.g. the CIO engine
    implementation("io.ktor:ktor-client-cio:KTOR_VERSION")
}
```

If you use multiple modules, you can use the bom dependency to get the correct versions for all
modules:

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

#### [Compose Auth](/plugins/ComposeAuth)

#### [Compose Auth UI](/plugins/ComposeAuthUI)

# Demos

- [Chat Demo (Desktop/Android/Browser)](https://github.com/supabase-community/supabase-kt/tree/master/demos/chat-demo-mpp)
- [File Upload Demo (Desktop/Android)](https://github.com/supabase-community/supabase-kt/tree/master/demos/file-upload)
- [Android Native Google login & in-app OAuth (Android)](https://github.com/supabase-community/supabase-kt/tree/master/demos/android-login)
- [Multi-Factor Authentication (Desktop/Android/Browser)](https://github.com/supabase-community/supabase-kt/tree/master/demos/multi-factor-authentication)
- [Multiplatform Deep Linking (Desktop/Android)](https://github.com/supabase-community/supabase-kt/tree/master/demos/multiplatform-deeplinks)
- [Groceries Store App (Android)](https://github.com/hieuwu/android-groceries-store)

# Need help?

- [Join the Supabase Discord](https://discord.supabase.com) and create a new post with the 'Kotlin' tag under `help-and-questions`
- [Create a discussion](https://github.com/supabase-community/supabase-kt/discussions/new/choose)
- [Create an issue](https://github.com/supabase-community/supabase-kt/issues/new/choose)

# Videos

- [Getting started with Supabase on Android](https://www.youtube.com/watch?v=SGr73sWMX6w) (by [Eric Ampire](https://www.youtube.com/@eric-ampire))
- [Supabase | Jetpack Compose | Android | 2023](https://www.youtube.com/playlist?list=PL91kV_wdjTlcGQdcZzkuid094as5eUlwU) (by [YoursSohailYT](https://www.youtube.com/@YoursSohailYT))

# Contribution

### How to contribute

1. Fork the repository
2. Create a branch
3. Make your changes
4. Submit a pull request with your new branch

# Credits

- Postgres Syntax inspired by https://github.com/supabase-community/postgrest-kt
- Plugin system inspired by ktor

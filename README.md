# supabase-kt

A Kotlin Multiplatform Client for Supabase.

For information about supported Kotlin targets, see the corresponding module README.

[Migrating from version 1.4.X to 2.0.0](/MIGRATION.md)

*Note: [WASM](https://github.com/supabase-community/supabase-kt/issues/86) build available: [2.1.1-wasm0](https://github.com/supabase-community/supabase-kt/releases/tag/2.0.2-wasm0)*

[![](https://img.shields.io/github/release/supabase-community/supabase-kt?label=stable)](https://github.com/supabase-community/supabase-kt/releases) [![](https://badgen.net/github/release/supabase-community/supabase-kt?label=prerelease)](https://central.sonatype.com/search?q=io.github.jan.supabase&smo=true)

### Links

[Documentation](https://supabase.com/docs/reference/kotlin/introduction)

[Getting started with Android and Supabase [Video]](https://www.youtube.com/watch?v=_iXUVJ6HTHU)

[Quickstart](https://supabase.com/docs/guides/getting-started/quickstarts/kotlin)

[Tutorial: Build a Product Management Android App with Jetpack Compose](https://supabase.com/docs/guides/getting-started/tutorials/with-kotlin)

[Dokka documentation for the latest version](https://supabase-community.github.io/supabase-kt/)

[Troubleshooting](https://github.com/supabase-community/supabase-kt/wiki/Troubleshooting)

# Installation

**Available modules**: `gotrue-kt`, `postgrest-kt`, `functions-kt`, `storage-kt`, `realtime-kt`, `apollo-graphql`, `compose-auth`, `compose-auth-ui`, `coil-integration`, `imageloader-integration`

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

#### [Apollo GraphQL integration](/plugins/ApolloGraphQL) - Creates an [Apollo GraphQL Client](https://github.com/apollographql/apollo-kotlin) for interacting with the Supabase API.

#### [Compose Auth](/plugins/ComposeAuth) - Provides easy Native Google & Apple Auth for Compose Multiplatform targets.

#### [Compose Auth UI](/plugins/ComposeAuthUI) - Provides UI Components for Compose Multiplatform.

#### [Coil Integration](/plugins/CoilIntegration) - Provides a [Coil](https://github.com/coil-kt/coil) Integration for displaying images stored in Supabase Storage.

#### [Compose-ImageLoader Integration](/plugins/ImageLoaderIntegration) - Provides a [Compose ImageLoader](https://github.com/qdsfdhvh/compose-imageloader) Integration for displaying images stored in Supabase Storage.

# Demos

- [Chat Demo (Desktop/Android/iOS/Browser)](https://github.com/supabase-community/supabase-kt/tree/master/demos/chat-demo-mpp)
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

- [Getting started with Android and Supabase](https://www.youtube.com/watch?v=_iXUVJ6HTHU) by the official Supabase YouTube channel
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

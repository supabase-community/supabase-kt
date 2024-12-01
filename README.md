# supabase-kt

A Kotlin Multiplatform Client for Supabase.

For information about supported Kotlin targets, see the corresponding module README.

[Migrating from version 2.X to 3.0.0](/MIGRATION.md)

*Note: The `WASM-JS` target for supported modules is only available for version 3.0.0 and above*

[![](https://img.shields.io/github/release/supabase-community/supabase-kt?label=stable)](https://github.com/supabase-community/supabase-kt/releases) 
[![](https://badgen.net/github/release/supabase-community/supabase-kt?label=prerelease)](https://central.sonatype.com/search?q=io.github.jan.supabase&smo=true) 
[![Kotlin](https://img.shields.io/badge/kotlin-2.0.21-blue.svg?logo=kotlin)](http://kotlinlang.org) 
[![Ktor](https://img.shields.io/badge/ktor-3.0.0-blue)](https://ktor.io/)
[![slack](https://img.shields.io/badge/slack-%23supabase--kt-purple.svg?logo=slack)](https://kotlinlang.slack.com/archives/C06QXPC7064)

### Links

[Documentation](https://supabase.com/docs/reference/kotlin/introduction)

[Samples](#samples)

[Getting started with Android and Supabase [Video]](https://www.youtube.com/watch?v=_iXUVJ6HTHU)

[Quickstart](https://supabase.com/docs/guides/getting-started/quickstarts/kotlin)

[Tutorial: Build a Product Management Android App with Jetpack Compose](https://supabase.com/docs/guides/getting-started/tutorials/with-kotlin)

[Dokka documentation for the latest version](https://supabase-community.github.io/supabase-kt/)

[Troubleshooting](/TROUBLESHOOTING)

# Installation

### Add one or more modules to your project

**Available modules**: `auth-kt`*, `postgrest-kt`, `functions-kt`, 
`storage-kt`, `realtime-kt`, `apollo-graphql`, `compose-auth`, 
`compose-auth-ui`, `coil-integration`, `coil3-integration`, `imageloader-integration`

```kotlin
dependencies {
    implementation("io.supabase:[module]:VERSION")
}
```

> [!NOTE]
> Before version 4.0.0, the Maven Central group id was `io.github.jan-tennert` instead of `io.supabase`
> \* Before version 3.0.0, the module was called `gotrue-kt`.

If you use multiple modules, you can use the bom dependency to get the correct versions for all
modules:

```kotlin
implementation(platform("io.supabase:bom:VERSION"))
implementation("io.supabase:[module]")
```

*Note that the minimum Android SDK version is 26. For lower versions, you need to enable [core library desugaring](https://developer.android.com/studio/write/java8-support#library-desugaring).*

### Add a Ktor Client Engine to each of your Kotlin targets

You can find a list of available engines [here](https://ktor.io/docs/http-client-engines.html).
If you plan to use the Realtime dependency, make sure to check if the engine supports WebSockets. See the [Ktor docs](https://ktor.io/docs/http-client-engines.html#limitations) for more information.


```kotlin
implementation("io.ktor:ktor-client-[engine]:VERSION")
```

<details>
<summary>Multiplatform Example</summary>

For targets: `jvm`, `android`, `js`, `ios`

```kotlin
val commonMain by getting {
    dependencies {
        //supabase modules
    }
}
val jvmMain by getting {
    dependencies {
        implementation("io.ktor:ktor-client-cio:KTOR_VERSION")
    }
}
val androidMain by getting {
    dependsOn(jvmMain)
}
val jsMain by getting {
    dependencies {
        implementation("io.ktor:ktor-client-js:KTOR_VERSION")
    }
}
val iosMain by getting {
    dependencies {
        implementation("io.ktor:ktor-client-darwin:KTOR_VERSION")
    }
}
```
</details>

**Note:** It is recommended to use the same Ktor version as supabase-kt:

__For 3.0.0 and above:__
[![Ktor](https://img.shields.io/badge/ktor-3.0.0-blue)](https://ktor.io/)

__For versions below 3.0.0:__
[![Ktor](https://img.shields.io/badge/ktor-2.3.12-blue)](https://ktor.io/)

## Main Modules

- [Authentication](/Auth)
- [Database/Postgrest](/Postgrest)
- [Storage](/Storage)
- [Realtime](/Realtime)
- [Functions (Edge Functions)](/Functions)

### Plugins

- [Apollo GraphQL integration](/plugins/ApolloGraphQL) - Creates an [Apollo GraphQL Client](https://github.com/apollographql/apollo-kotlin) for interacting with the Supabase API.
- [Compose Auth](/plugins/ComposeAuth) - Provides easy Native Google & Apple Auth for Compose Multiplatform targets.
- [Compose Auth UI](/plugins/ComposeAuthUI) - Provides UI Components for Compose Multiplatform.
- [Coil Integration](/plugins/CoilIntegration) - Provides a [Coil2](https://github.com/coil-kt/coil) Integration for displaying images stored in Supabase Storage. Only supports Android.
- [Coil3 Integration](/plugins/Coil3Integration) - Provides a [Coil3](https://github.com/coil-kt/coil) Integration for displaying images stored in Supabase Storage. Supports all Compose Multiplatform targets.
- [Compose-ImageLoader Integration](/plugins/ImageLoaderIntegration) - Provides a [Compose ImageLoader](https://github.com/qdsfdhvh/compose-imageloader) Integration for displaying images stored in Supabase Storage.

### Miscellaneous
- [Supabase Edge Functions Kotlin](https://github.com/manriif/supabase-edge-functions-kt) - Build, serve and deploy Supabase Edge Functions with Kotlin and Gradle.

# Samples

### Multiplatform Applications

**Official Samples**

- [Chat Demo](/sample/chat-demo-mpp) *(Desktop/Android/iOS/Browser)* - A simple chat application using the Auth, Postgrest and Realtime module.
- [File Upload Demo](/sample/file-upload) *(Desktop/Android)* - A simple bulk file upload application using the Storage module.
- [Multi-Factor Authentication](/sample/multi-factor-auth) *(Desktop/Android/Browser)* - A simple application showcasing Multi-Factor Authentication.

**Community Samples**

- [Groceries Store App](https://github.com/hieuwu/android-groceries-store) *(Android)*
 
### Server-side Applications

**Community Samples**

- [RESTful Service](https://github.com/hieuwu/supa-spring-kt) *(Spring Boot)*

# Need help?

- Join the [#supabase-kt](https://kotlinlang.slack.com/archives/C06QXPC7064) Kotlin Slack channel
- [Join the Supabase Discord](https://discord.supabase.com) and create a new post with the 'Kotlin' tag under `help-and-questions`
- [Create a discussion](https://github.com/supabase-community/supabase-kt/discussions/new/choose)
- [Create an issue](https://github.com/supabase-community/supabase-kt/issues/new/choose)

# Videos

- [Getting started with Android and Supabase](https://www.youtube.com/watch?v=_iXUVJ6HTHU) by the official Supabase YouTube channel
- [Sign in with Google on Android using Credential Manager](https://www.youtube.com/watch?v=P_jZMDmodG4) by the official Supabase YouTube channel
- [Getting started with Supabase on Android](https://www.youtube.com/watch?v=SGr73sWMX6w) (by [Eric Ampire](https://www.youtube.com/@eric-ampire))
- [Supabase | Jetpack Compose | Android | 2023](https://www.youtube.com/playlist?list=PL91kV_wdjTlcGQdcZzkuid094as5eUlwU) (by [YoursSohailYT](https://www.youtube.com/@YoursSohailYT))

# Contribution

Checkout the [contribution guidelines](/CONTRIBUTING.md) for more information.

# Credits

- Postgres Syntax inspired by https://github.com/supabase-community/postgrest-kt
- Plugin system inspired by ktor

## Sponsoring

If you like the project and want to support it, consider sponsoring me on [Ko-fi](https://ko-fi.com/jantennert) or directly on [PayPal](https://www.paypal.com/donate/?hosted_button_id=SR3YJS5CZFS9L).

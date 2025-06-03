# Supabase-kt Coil 3 Integration

Extends supabase-kt with a [Sketch](https://github.com/panpf/sketch) integration for image loading.
**Requires supabase-kt `3.2.0` or higher.**

Current supported Sketch version: `4.1.0` and higher.

Supported targets:

| Target | **JVM** | **Android** | **JS** | **Wasm** | **iOS** |
|--------|---------|-------------|--------|----------|---------|
| Status | ✅       | ✅           | ✅      | ✅        | ✅       |

<details>

<summary>In-depth Kotlin targets</summary>

**JS**: Browser

**Wasm**: wasm-js

**iOS**: iosArm64, iosSimulatorArm64, iosX64

</details>

# Installation

Newest version: [![](https://img.shields.io/github/release/supabase-community/supabase-kt?label=)](https://github.com/supabase-community/supabase-kt/releases)

```kotlin
dependencies {
    implementation("io.github.jan-tennert.supabase:sketch-integration:VERSION")
}
```

Install plugin in main SupabaseClient. See the [documentation](https://supabase.com/docs/reference/kotlin/initializing) for more information
```kotlin
val client = createSupabaseClient(
    supabaseUrl = "https://id.supabase.co",
    supabaseKey = "apikey"
) {
    //...
    install(Storage) {
        //your config
    }
    install(SketchIntegration)
}
```

If you don't have a sketch-http artifact in your dependencies, you will need to add it. See the [Sketch documentation](https://github.com/panpf/sketch#install) for more information.

# Usage

### Add Supabase Fetcher to Sketch

Create Sketch with the Supabase Fetcher.

```kotlin
Sketch.Builder(it).apply {
    components {
        supportSupabaseStorage(supabase)
    }
}.build()
```

You can also replace the default Sketch object
For Compose Multiplatform Applications using the `coil-compose` dependency, you can use the `setSingletonImageLoaderFactory` composable function:
```kotlin
setSingletonImageLoaderFactory { platformContext ->
    ImageLoader.Builder(platformContext)
        .components {
            add(supabaseClient.coil3)
            //Your network fetcher factory
            add(KtorNetworkFetcherFactory())
        }
        .build()
}
```
You call this composable before any `Image` composable is used. Presumably in your `Root` composable.

See the [Coil documentation](https://coil-kt.github.io/coil/getting_started/#image-loaders) for more information.

### Display images from Supabase Storage

You can easily create an image request like this:

```kotlin
val request = ImageRequest.Builder(context)
    .data(authenticatedStorageItem("icons", "profile.png")) //for non-public buckets
    .build()
```

Or if you are using [Compose Multiplatform](https://coil-kt.github.io/coil/compose/):

```kotlin
AsyncImage(
    model = publicStorageItem("icons", "profile.png"), //for public buckets
    contentDescription = null,
)
```

The Coil integration will automatically add the Authorization header to the request if the bucket is not public.
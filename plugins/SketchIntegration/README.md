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
```kotlin
SingletonSketch.setSafe {
    Sketch.Builder(it).apply {
        components {
            supportSupabaseStorage(supabase)
        }
    }.build()
}
```

See the [Sketch documentation](https://github.com/panpf/sketch/blob/main/docs/getting_started.md#singleton-mode) for more information.

### Display images from Supabase Storage

You can easily create an image request like this:

```kotlin
val request = ImageRequest(context, authenticatedStorageItem("icons", "profile.png").asSketchUri())
```

Or if you are using [Sketch Compose](https://github.com/panpf/sketch/blob/main/docs/compose.md):

```kotlin
AsyncImage(
    uri = authenticatedStorageItem("icons", "profile.png").asSketchUri(),
    contentDescription = "profile image"
)
```

The Sketch integration will automatically add the Authorization header to the request if the bucket is not public.
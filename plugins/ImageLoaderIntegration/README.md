# Supabase-kt Compose-ImageLoader Integration

Extends supabase-kt with a [Compose-ImageLoader](https://github.com/qdsfdhvh/compose-imageloader) integration for image loading.

Supported targets:

| Target | **JVM** | **Android** | **JS** | **iOS** |
|--------|---------|-------------|--------|---------|
| Status | ✅       | ✅           | ✅      | ✅       |

> Wasm-JS is currently not supported due to a bug in the Compose-ImageLoader library.

<details>

<summary>In-depth Kotlin targets</summary>

**JS**: Browser

**iOS**: iosArm64, iosSimulatorArm64, iosX64

</details>

# Installation

Newest version: [![](https://img.shields.io/github/release/supabase-community/supabase-kt?label=)](https://github.com/supabase-community/supabase-kt/releases)

```kotlin
dependencies {
    implementation("io.github.jan-tennert.supabase:imageloader-integration:VERSION")
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
    install(ImageLoaderIntegration)
}
```

# Usage

### Add Supabase Fetcher and Keyer to Compose-ImageLoader

Create a new ImageLoader with the Supabase Fetcher and Keyer. See the [Compose-ImageLoader documentation](https://github.com/qdsfdhvh/compose-imageloader) for more information.

> The fetcher is used to download the data and the keyer is used to allow in-memory and disk caching

```kotlin
ImageLoader {
    //...
    components {
        add(keyer = supabaseClient.imageLoader)
        add(fetcherFactory = supabaseClient.imageLoader)
    }
    //...
}
```

### Display images from Supabase Storage

You can easily display images from Supabase Storage like this:

```kotlin
AutoSizeImage(
    request = remember { ImageRequest(authenticatedStorageItem("icons", "user.png")) }, //or use publicStorageItem("icons", "user.png") for public buckets
    contentDescription = null
)
```

The ImageLoader integration will automatically add the Authorization header to the request if the bucket is not public.

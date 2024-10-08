# Supabase-kt Coil Integration

Extends supabase-kt with a Coil integration for image loading.

Supported targets:

| Target | **Android** |
|--------|-------------|
| Status | ✅           |

For Compose Multiplatform support, [checkout the Coil 3 integration](/plugins/Coil3Integration) 

# Installation

Newest version: [![](https://img.shields.io/github/release/supabase-community/supabase-kt?label=)](https://github.com/supabase-community/supabase-kt/releases)

```kotlin
dependencies {
    implementation("io.github.jan-tennert.supabase:coil-integration:VERSION")
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
    install(CoilIntegration)
}
```

# Usage

### Add Supabase Fetcher to Coil

Create a new ImageLoader with the Supabase Fetcher. See the [Coil documentation](https://coil-kt.github.io/coil/image_pipeline/) for more information.

```kotlin
ImageLoader.Builder(context)
    .components {
        add(supabaseClient.coil)
    }
    .build()
```

You can also replace the default Coil Image Loader in your application. See the [Coil documentation](https://coil-kt.github.io/coil/getting_started/#image-loaders) for more information.

### Display images from Supabase Storage

You can easily create an image request like this:

```kotlin
val request = ImageRequest.Builder(context)
    .data(authenticatedStorageItem("icons", "profile.png")) //for non-public buckets
    .build()
```

Or if you are using [Jetpack Compose](https://coil-kt.github.io/coil/compose/):

```kotlin
AsyncImage(
    model = publicStorageItem("icons", "profile.png"), //for public buckets
    contentDescription = null,
)
```

The Coil integration will automatically add the Authorization header to the request if the bucket is not public.
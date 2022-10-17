# Supabase-kt Storage

Extends Supabase-kt with a multiplatform Storage client.

Newest version: [![Maven Central](https://img.shields.io/maven-central/v/io.github.jan-tennert.supabase/supabase-kt)](https://search.maven.org/search?q=g%3Aio.github.jan-tennert.supabase)

# Installation

```kotlin
dependencies {
    implementation("io.github.jan-tennert.supabase:storage-kt:VERSION")
}
```

Install plugin in main supabase client. See [supabase-kt](https://github.com/supabase-community/supabase-kt) for more information
```kotlin
val client = createSupabaseClient {
    
    //...
    
    install(Storage) {
        // settings
    }
    
}
```

# Features

<details><summary>Managing buckets</summary>

```kotlin
//create a bucket
client.storage.createBucket(name = "images", id = "images", public = false)

//empty bucket
client.storage.emptyBucket(id = "images")

//and so on
```

</details>

<details><summary>Uploading files</summary>

```kotlin
val bucket = client.storage["images"]

//upload a file (jvm)
bucket.upload("landscape.png", File("landscape.png"))

//download a file (jvm)
bucket.downloadTo("landscape.png", File("landscape.png"))

//copy a file

bucket.copy("landscape.png", "landscape2.png")

//and so on
```

</details>
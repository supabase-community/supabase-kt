# Supabase-kt Functions

Extends Supabase-kt with a multiplatform Functions client.

Newest version: [![Maven Central](https://img.shields.io/maven-central/v/io.github.jan-tennert.supabase/supabase-kt)](https://search.maven.org/search?q=g%3Aio.github.jan-tennert.supabase)

# Installation

```kotlin
dependencies {
    implementation("io.github.jan-tennert.supabase:functions-kt:VERSION")
}
```

Install plugin in main supabase client. See [supabase-kt](https://github.com/supabase-community/supabase-kt) for more information
```kotlin
val client = createSupabaseClient {
    
    //...
    
    install(Functions) {
        // settings
    }
    
}
```

# Features

<details><summary>Execute edge functions directly</summary>

```kotlin
@Serializable
data class SomeData(val name: String)

val response: HttpResponse = client.functions("test")
//with body
val response: HttpResponse = client.functions(
    function = "test",
    body = SomeData("Name")
    headers = Headers.build {
        append(HttpHeaders.ContentType, "application/json")
    }
)
```
</details>
<details><summary>Store your edge function in a variable</summary>

```kotlin
@Serializable
data class SomeData(val name: String)

val testFunction: EdgeFunction = client.functions.buildEdgeFunction {
    functionName = "test"
    headers.append(HttpHeaders.ContentType, "application/json")
}

val response: HttpResponse = testFunction()
//with body
val response: HttpResponse = testFunction(SomeData("Name"))
```
</details>
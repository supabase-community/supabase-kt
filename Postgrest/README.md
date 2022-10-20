# Supabase-kt Postgrest

Extends Supabase-kt with a multiplatform Postgrest client.

Newest version: [![Maven Central](https://img.shields.io/maven-central/v/io.github.jan-tennert.supabase/supabase-kt)](https://search.maven.org/search?q=g%3Aio.github.jan-tennert.supabase)

# Installation

```kotlin
dependencies {
    implementation("io.github.jan-tennert.supabase:postgrest-kt:VERSION")
}
```

Install plugin in main supabase client. See [supabase-kt](https://github.com/supabase-community/supabase-kt) for more information
```kotlin
val client = createSupabaseClient {
    
    //...
    
    install(Postgrest) {
        // settings
    }
    
}
```

# Features

<details><summary>Make database calls</summary>

```kotlin
//a data class for a message

data class Message(val text: String, @SerialName("author_id") val authorId: String, val id: Int)

```

<b>If you use the syntax with property references the client will automatically look for @SerialName annotiations on your class property and if it has one it will use the value as the column name. (Only JVM)</b>

<blockquote>

<details><summary>Select</summary>

```kotlin
val result = client.postgrest["messages"]
    .select {
        //you can use that syntax
        Message::authorId eq "someid"
        Message::text neq "This is a text!"
        Message::authorId isIn listOf("test", "test2")

        //or this. But they are the same
        eq("author_id", "someid")
        neq("text", "This is a text!")
        isIn("author_id", listOf("test", "test2"))
    }

println(result.decodeList<Message>())
````

</details>

<details><summary>Insert</summary>

```kotlin
client.postgrest["messages"]
    .insert(Message("This is a text!", "someid", 1))
````

</details>

<details><summary>Update</summary>

```kotlin
client.postgrest["messages"]
    .update(
        {
            Message::text setTo "This is the edited text!"
        }
    ) {
        Message::id eq 2
    }
````

</details>

<details><summary>Delete</summary>

```kotlin
client.postgrest["messages"]
    .delete {
        Message::id eq 2
    }
````

</details>

</blockquote>

</details>

<details><summary>Execute database functions</summary>

```kotlin
client.postgrest.rpc("do_something")
//with parameters and filter
client.postgrest.rpc("do_something", mapOf("param1" to "value1")) {
    eq("id", 1) 
}
```

</details>
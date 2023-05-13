# Supabase-kt ApolloGraphQL

Extends Supabase-kt with an Apollo GraphQL Client.

Newest version: [![Maven Central](https://img.shields.io/maven-central/v/io.github.jan-tennert.supabase/supabase-kt)](https://search.maven.org/search?q=g%3Aio.github.jan-tennert.supabase)

# Installation

```kotlin
dependencies {
    implementation("io.github.jan-tennert.supabase:apollo-graphql:VERSION")
}
```

Install plugin in main SupabaseClient. See the [documentation](https://supabase.com/docs/reference/kotlin/initializing) for more information
```kotlin
val client = createSupabaseClient(
    supabaseUrl = "https://id.supabase.co",
    supabaseKey = "apikey"
) {
    //...
    
    install(GraphQL) {
        apolloConfiguration {
            // settings
        }
    }
    
}
```

# Usage

The plugin automatically creates an Apollo Client with the corresponding headers for Authorization depending on your session.

To access the client, you can use the `apolloClient` property of the `GraphQL` plugin instance.

```kotlin
client.graphql.apolloClient.query(YourQuery()).execute().data //execute a query
```

To learn about how to use Apollo GraphQL, see [Apollo Kotlin](https://github.com/apollographql/apollo-kotlin#getting-started).
# Supabase-kt ApolloGraphQL

Extends Supabase-kt with an Apollo GraphQL Client.

Newest version: [![Maven Central](https://img.shields.io/maven-central/v/io.github.jan-tennert.supabase/supabase-kt)](https://search.maven.org/search?q=g%3Aio.github.jan-tennert.supabase)

# Installation

```kotlin
dependencies {
    implementation("io.github.jan-tennert.supabase:apollo-graphql:VERSION")
}
```

Install plugin in main supabase client. See [Getting started](https://github.com/supabase-community/supabase-kt/wiki/Getting-Started) for more information
```kotlin
val client = createSupabaseClient(supabaseUrl, supabaseKey) {
    
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
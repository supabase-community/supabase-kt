# Supabase-kt ApolloGraphQL

Extends Supabase-kt with an Apollo GraphQL Client.

Supported targets:

| Target | **JVM** | **Android** | **JS** | **iOS** | **tvOS** | **watchOS** | **macOS** | **Windows** | **Linux** |
|--------|---------|-------------|--------|---------|----------|-------------|-----------|-------------|-----------|
|        | ✅       | ✅           | ✅      | ✅       | ✅        | ✅           | ✅         | ❌           | ❌         |

<details>

<summary>In-depth Kotlin targets</summary>

**iOS:** iosArm64, iosSimulatorArm64, iosX64

**JS**: Browser, NodeJS

**tvOS**: tvosArm64, tvosX64, tvosSimulatorArm64

**watchOS**: watchosArm64, watchosX64, watchosSimulatorArm64

**MacOS**: macosX64, macosArm64

**Windows**: mingwX64

**Linux**: linuxX64

</details>

# Installation

Newest version: [![](https://img.shields.io/github/release/supabase-community/supabase-kt?label=)](https://github.com/supabase-community/supabase-kt/releases)

```kotlin
dependencies {
    implementation("io.github.jan-tennert.supabase:apollo-graphql:VERSION")
}
```

Install the plugin in your SupabaseClient. See the [documentation](https://supabase.com/docs/reference/kotlin/initializing) for more information
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
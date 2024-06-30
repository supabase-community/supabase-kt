pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("0.8.0")
}

// Main Modules
include("GoTrue")
include("Postgrest")
include("Storage")
include("Realtime")
include("Functions")
include("bom")

// Test module
include("test-common")

// Serializers
include(":serializers:Moshi")
project(":serializers:Moshi").name = "serializer-moshi"
include(":serializers:Jackson")
project(":serializers:Jackson").name = "serializer-jackson"

// Misc plugins
include(":plugins:ApolloGraphQL")
include(":plugins:ComposeAuth")
include(":plugins:ComposeAuthUI")
include(":plugins:CoilIntegration")
include(":plugins:ImageLoaderIntegration")

// Samples
println("LibrariesOnly: ${System.getProperty("LibrariesOnly")}")
if (System.getProperty("LibrariesOnly") != "true") {
    include(":sample:chat-demo-mpp:common")
    include(":sample:chat-demo-mpp:web")
    include(":sample:chat-demo-mpp:ios")
    include(":sample:chat-demo-mpp:desktop")
    include(":sample:chat-demo-mpp:android")
}

// Renames
project(":GoTrue").name = "gotrue-kt"
project(":Postgrest").name = "postgrest-kt"
project(":Storage").name = "storage-kt"
project(":Realtime").name = "realtime-kt"
project(":Functions").name = "functions-kt"
project(":plugins:ApolloGraphQL").name = "apollo-graphql"
project(":plugins:ComposeAuth").name = "compose-auth"
project(":plugins:ComposeAuthUI").name = "compose-auth-ui"
project(":plugins:CoilIntegration").name = "coil-integration"
project(":plugins:ImageLoaderIntegration").name = "imageloader-integration"
rootProject.name = "supabase-kt"


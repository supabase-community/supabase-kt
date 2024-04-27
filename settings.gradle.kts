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

include("GoTrue")
include("Postgrest")
include("Storage")
include("Realtime")
include("Functions")
include("bom")

include("test")
include("test-w")
include("test-common")

include(":serializers:Moshi")
project(":serializers:Moshi").name = "serializer-moshi"
include(":serializers:Jackson")
project(":serializers:Jackson").name = "serializer-jackson"

include(":plugins:ApolloGraphQL")
include(":plugins:ComposeAuth")
include(":plugins:ComposeAuthUI")
include(":plugins:CoilIntegration")
include(":plugins:ImageLoaderIntegration")
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


pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

include("GoTrue")
include("Postgrest")
include("Storage")
include("Realtime")
include("Functions")
include("bom")

include("test")
include("test-w")

include(":serializers:KotlinX")
project(":serializers:KotlinX").name = "serializer-kotlinx"
include(":serializers:Moshi")
project(":serializers:Moshi").name = "serializer-moshi"
include(":serializers:Jackson")
project(":serializers:Jackson").name = "serializer-jackson"

include(":plugins:ApolloGraphQL")
project(":GoTrue").name = "gotrue-kt"
project(":Postgrest").name = "postgrest-kt"
project(":Storage").name = "storage-kt"
project(":Realtime").name = "realtime-kt"
project(":Functions").name = "functions-kt"
project(":plugins:ApolloGraphQL").name = "apollo-graphql"
rootProject.name = "supabase-kt"


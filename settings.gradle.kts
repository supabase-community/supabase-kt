
pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("0.10.0")
}

// Main Modules
include("Auth")
include("Postgrest")
include("Storage")
include("Realtime")
include("Functions")
include("Supabase")
include("bom")

// Test module
include("test-common")

// Serializers
include(":serializers:Moshi")
project(":serializers:Moshi").name = "serializer-moshi"
include(":serializers:Jackson")
project(":serializers:Jackson").name = "serializer-jackson"

// Samples
if (System.getProperty("LibrariesOnly") != "true") {
    includeSample("chat-demo-mpp", "common", "web", "ios", "desktop", "android")
    includeSample("file-upload", "common", "desktop", "android")
    includeSample("multi-factor-auth", "common", "desktop", "android", "web")
}

// Renames
project(":Auth").name = "auth-kt"
project(":Postgrest").name = "postgrest-kt"
project(":Storage").name = "storage-kt"
project(":Realtime").name = "realtime-kt"
project(":Functions").name = "functions-kt"
project(":Supabase").name = "supabase-kt"
rootProject.name = "supabase-kt"

fun includeSample(name: String, vararg targets: String) {
    targets.forEach {
        include(":sample:$name:$it")
    }
}

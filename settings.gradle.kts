pluginManagement {
    repositories {
        google()
        jcenter()
        gradlePluginPortal()
        mavenCentral()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.namespace == "com.android" || requested.id.name == "kotlin-android-extensions") {
                useModule("com.android.tools.build:gradle:7.0.0")
            }
        }
    }
}

include("GoTrue")
include("Postgrest")
//include(":desktopDemo")
include(":androidDemo")
//include(":webDemo")
include("Storage")
include("Realtime")
include("Functions")
include("bom")
project(":GoTrue").name = "gotrue-kt"
project(":Postgrest").name = "postgrest-kt"
project(":Storage").name = "storage-kt"
project(":Realtime").name = "realtime-kt"
project(":Functions").name = "functions-kt"
rootProject.name = "supabase-kt"


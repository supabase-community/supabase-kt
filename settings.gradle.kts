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

include("Auth")
include("Postgrest")
//include(":desktopDemo")
//include(":androidDemo")
//include(":webDemo")
include("Storage")
include("Realtime")
include("Functions")
project(":Auth").name = "Supacompose-Auth"
project(":Postgrest").name = "Supacompose-Postgrest"
project(":Storage").name = "Supacompose-Storage"
project(":Realtime").name = "Supacompose-Realtime"
project(":Functions").name = "Supacompose-Functions"
rootProject.name = "Supacompose"


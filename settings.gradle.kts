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
include(":desktopDemo")
include(":androidDemo")
include(":webDemo")
project(":Auth").name = "Supacompose-Auth"
rootProject.name = "Supacompose"


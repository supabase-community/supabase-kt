plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose") version Versions.COMPOSE
}

repositories {
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev/")
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
        name = "ktor-eap"
    }
}

group = "io.github.jan-tennert.supacompose.web"
version = "1.0"

kotlin {
    js("web", IR) {
        browser()
        binaries.executable()
    }
    sourceSets {
        val webMain by getting {
            dependencies {
                implementation(project(":gotrue-kt"))
                api(compose.web.core)
                api(compose.runtime)
            }
        }
    }
}
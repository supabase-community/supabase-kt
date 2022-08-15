plugins {
    kotlin("multiplatform") version "1.6.21"
    id("org.jetbrains.compose") version "1.2.0-alpha01-dev686"
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
                val supacompose = "0.1.0"
                implementation("io.github.jan-tennert.supacompose:Supacompose-Auth:$supacompose")
                api(compose.web.core)
                api(compose.runtime)
            }
        }
    }
}
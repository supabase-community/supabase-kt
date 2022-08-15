import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

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

group = "io.github.jan-tennert.supacompose.desktop"
version = "1.0"

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
        withJava()
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                val supacompose = "0.1.0"
                implementation("io.github.jan-tennert.supacompose:Supacompose-Storage:$supacompose")
                implementation("io.github.jan-tennert.supacompose:Supacompose-Postgrest:$supacompose")
                implementation("io.github.jan-tennert.supacompose:Supacompose-Realtime:$supacompose")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
                implementation(compose.runtime)
                implementation(compose.ui)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.desktop.currentOs)
                implementation("io.ktor:ktor-client-cio:2.0.3")
                //implement logcat-classic
                implementation("ch.qos.logback:logback-classic:1.2.11")
                // https://mvnrepository.com/artifact/io.ktor/ktor-server-core
               // implementation("io.ktor:ktor-server-okhttp:2.0.1")
            }
        }
        val jvmTest by getting
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "SupaCompose"
            packageVersion = "1.0.0"
        }
    }
}

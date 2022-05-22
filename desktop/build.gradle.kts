import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

repositories {
    mavenCentral()
}

group = "io.github.jan-tennert.supacompose.desktop"
version = Versions.SUPABASE

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
                implementation(project(":Supacompose-Auth"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.COROUTINES}")

                implementation(compose.desktop.currentOs)
                implementation("io.ktor:ktor-client-cio:${Versions.KTOR}")
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

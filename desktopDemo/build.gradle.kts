import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

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

group = "io.github.jan-tennert.supabae.desktop"
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
                api(project(":gotrue-kt"))
                implementation("com.russhwolf:multiplatform-settings:${Versions.SETTINGS}")
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
            packageName = "SupabaseKt"
            packageVersion = "1.0.0"
        }
    }
}

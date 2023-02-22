import java.net.URL

buildscript {
    dependencies {
        classpath("org.jetbrains.kotlinx:atomicfu-gradle-plugin:${Versions.ATOMICFU}")
    }
}

plugins {
    kotlin("multiplatform") version Versions.KOTLIN
    id("com.android.library")
    id("maven-publish")
    signing
    id("org.jetbrains.dokka") version Versions.DOKKA
    id("io.codearte.nexus-staging") version Versions.NEXUS_STAGING
    kotlin("plugin.serialization") version Versions.KOTLIN
 //   id("org.jetbrains.compose") version Versions.COMPOSE
}

val modules = listOf("supabase-kt", "gotrue-kt", "postgrest-kt", "storage-kt", "realtime-kt", "functions-kt", "apollo-graphql")

allprojects {
    repositories {
        google()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev/")
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
            name = "ktor-eap"
        }
    }
}

configure(allprojects.filter { it.name in modules || it.name == "bom" }) {
    apply(plugin = "signing")
    apply(plugin = "maven-publish")
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
    apply(plugin = "kotlinx-atomicfu")
    //apply(plugin = "com.android.library")
  //  apply(plugin = "org.jetbrains.compose")
}

nexusStaging {
    stagingProfileId = Publishing.PROFILE_ID
    stagingRepositoryId.set(Publishing.REPOSITORY_ID)
    username = Publishing.SONATYPE_USERNAME
    password = Publishing.SONATYPE_PASSWORD
    serverUrl = "https://s01.oss.sonatype.org/service/local/"
}

configure(allprojects.filter { it.name in modules }) {
    signing {
        val signingKey = providers
            .environmentVariable("GPG_SIGNING_KEY")
            .orElse(File(System.getenv("GPG_PATH") ?: "").let {
                try {
                    it.readText()
                } catch(_: Exception) {
                    ""
                }
            })
            .forUseAtConfigurationTime()
        val signingPassphrase = providers
            .environmentVariable("GPG_SIGNING_PASSPHRASE")
            .forUseAtConfigurationTime()

        if (signingKey.isPresent && signingPassphrase.isPresent) {
            useInMemoryPgpKeys(signingKey.get(), signingPassphrase.get())
            val extension = extensions
                .getByName("publishing") as PublishingExtension
            sign(extension.publications)
        }
    }
    publishing {
        repositories {
            maven {
                name = "Oss"
                setUrl {
                    "https://s01.oss.sonatype.org/service/local/staging/deployByRepositoryId/${Publishing.REPOSITORY_ID}"
                }
                credentials {
                    username = Publishing.SONATYPE_USERNAME
                    password = Publishing.SONATYPE_PASSWORD
                }
            }
            maven {
                name = "Snapshot"
                setUrl { "https://s01.oss.sonatype.org/content/repositories/snapshots/" }
                credentials {
                    username = Publishing.SONATYPE_USERNAME
                    password = Publishing.SONATYPE_PASSWORD
                }
            }
        }
//val dokkaOutputDir = "H:/Programming/Other/DiscordKMDocs"
        val dokkaOutputDir = "$buildDir/dokka/${this@configure.name}"

        tasks.dokkaHtml {
            outputDirectory.set(file(dokkaOutputDir))
        }

        val deleteDokkaOutputDir by tasks.register<Delete>("deleteDokkaOutputDirectory") {
            delete(dokkaOutputDir)
        }

        val javadocJar = tasks.register<Jar>("javadocJar") {
            dependsOn(deleteDokkaOutputDir, tasks.dokkaHtml)
            archiveClassifier.set("javadoc")
            from(dokkaOutputDir)
        }

        tasks.withType<org.jetbrains.dokka.gradle.DokkaTaskPartial>().configureEach {
            dokkaSourceSets.configureEach {
                sourceLink {
                    val name = when(moduleName.get()) {
                        "functions-kt" -> "Functions"
                        "gotrue-kt" -> "GoTrue"
                        "postgrest-kt" -> "Postgrest"
                        "realtime-kt" -> "Realtime"
                        "storage-kt" -> "Storage"
                        else -> ""
                    }
                    localDirectory.set(projectDir.resolve("src"))
                    remoteUrl.set(URL("https://github.com/supabase-community/supabase-kt/tree/master/$name/src"))
                    remoteLineSuffix.set("#L")
                }
            }
        }

        publications {
            withType<MavenPublication> {
                artifact(javadocJar)
                pom {
                    name.set(this@configure.name)
                    description.set(this@configure.description ?: "A Kotlin Multiplatform Supabase Framework")
                    url.set("https://github.com/supabase-community/supabase-kt")
                    licenses {
                        license {
                            name.set("GPL-3.0")
                            url.set("https://www.gnu.org/licenses/gpl-3.0.en.html")
                        }
                    }
                    issueManagement {
                        system.set("Github")
                        url.set("https://github.com/supabase-community/supabase-kt/issues")
                    }
                    scm {
                        connection.set("https://github.com/supabase-community/supabase-kt.git")
                        url.set("https://github.com/supabase-community/supabase-kt")
                    }
                    developers {
                        developer {
                            name.set("TheRealJanGER")
                            email.set("jan.m.tennert@gmail.com")
                        }
                    }
                }
            }
        }
    }
}

group = "io.github.jan-tennert.supabase"
version = Versions.SUPABASEKT

kotlin {
    jvm {
        jvmToolchain(11)
        compilations.all {
            kotlinOptions.freeCompilerArgs = listOf(
                "-Xjvm-default=all",  // use default methods in interfaces,
                "-Xlambdas=indy"      // use invokedynamic lambdas instead of synthetic classes
            )
        }
    }
    android {
        publishLibraryVariants("release", "debug")
    }
    js(IR) {
        browser {
            testTask {
                enabled = false
                /**useKarma {
                    useFirefox()
                }*/
            }
        }
    }
    //ios()
    sourceSets {
        all {
            languageSettings.optIn("kotlin.RequiresOptIn")
        }
        val commonMain by getting {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-datetime:${Versions.DATETIME}")
                api("io.ktor:ktor-client-core:${Versions.KTOR}")
                api("io.ktor:ktor-client-content-negotiation:${Versions.KTOR}")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.COROUTINES}")
                api("io.ktor:ktor-serialization-kotlinx-json:${Versions.KTOR}")
                api("io.github.aakira:napier:${Versions.NAPIER}")
                api("org.jetbrains.kotlinx:atomicfu:${Versions.ATOMICFU}")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("io.ktor:ktor-client-mock:${Versions.KTOR}")
                // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-coroutines-test
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.COROUTINES}")
            }
        }
        val jvmMain by getting {
        }
        val jvmTest by getting
        val androidMain by getting {
            dependencies {
                //add android lifecycle
                api("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1")
                api("androidx.lifecycle:lifecycle-process:2.5.1")
                api("androidx.core:core-ktx:${Versions.ANDROID_CORE}")
                api("androidx.appcompat:appcompat:${Versions.ANDROID_COMPAT}")
            }
        }
        val androidUnitTest by getting {
            dependencies {
                implementation("junit:junit:4.13.2")
            }
        }
        val jsMain by getting {
            dependencies {
              //  api(compose.web.core)
            }
        }
    }
}

android {
    compileSdk = 33
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 21
    }
    lint {
        abortOnError = false
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
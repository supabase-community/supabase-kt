plugins {
    `java-platform`
}

group = "io.github.jan-tennert.supabase"
version = Versions.SUPABASEKT
description = "A Kotlin Multiplatform Supabase Framework"

val bomProject = project

val excludedModules = listOf("demo")

fun shouldIncludeInBom(candidateProject: Project) =
    excludedModules.all { !candidateProject.name.contains(it) } &&
            candidateProject.name != bomProject.name

rootProject.subprojects.filter(::shouldIncludeInBom).forEach { bomProject.evaluationDependsOn(it.path) }

dependencies {
    constraints {
        rootProject.subprojects.filter { project ->
            // Only declare dependencies on projects that will have publications
            shouldIncludeInBom(project) && project.tasks.findByName("publish")?.enabled == true
        }.forEach { api(project(it.path)) }
    }
}

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

    publications {
        create<MavenPublication>("bom") {
            from(components["javaPlatform"])
            pom {
                name.set("supabase-kt")
                description.set("A Kotlin Multiplatform Supabase Framework")
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


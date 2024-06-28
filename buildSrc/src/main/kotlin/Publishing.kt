import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.authentication.http.BasicAuthentication
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.repositories

fun Project.applyPublishing() {
    extensions.configure(MavenPublishBaseExtension::class) {
        publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.S01)

        signAllPublications()
        coordinates(extra["base-group"].toString(), this@applyPublishing.name, extra["supabase-version"].toString())

        pom {
            name.set(this@applyPublishing.name)
            description.set(this@applyPublishing.description ?: "A Kotlin Multiplatform Supabase SDK")
            inceptionYear.set("2024")
            url.set("https://github.com/supabase-community/supabase-kt/")
            licenses {
                license {
                    name.set("MIT License")
                    url.set("https://github.com/supabase-community/supabase-kt/blob/master/LICENSE")
                    distribution.set("https://github.com/supabase-community/supabase-kt/blob/master/LICENSE")
                }
            }
            developers {
                developer {
                    id.set("TheRealJan")
                    name.set("Jan Tennert")
                    url.set("https://github.com/jan-tennert/")
                }
            }
            scm {
                url.set("https://github.com/supabase-community/supabase-kt/")
                connection.set("scm:git:git://github.com/supabase-community/supabase-kt.git")
                developerConnection.set("scm:git:ssh://git@github.com/supabase-community/supabase-kt.git")
            }
        }
        repositories {
            maven {
                name = "SonatypeMaven"
                sonatypeAuth()
            }
        }
    }
}

fun MavenArtifactRepository.sonatypeAuth() {
    credentials {
        username = System.getenv("TOKEN_NAME") as String
        password = System.getenv("TOKEN_KEY") as String
    }
    authentication {
        create<BasicAuthentication>("basic")
    }
}
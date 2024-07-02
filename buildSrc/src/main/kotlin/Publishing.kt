import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.extra

val Project.supabaseVersion get() = System.getProperty("SupabaseVersion") ?: extra["supabase-version"].toString()

fun Project.applyPublishing() {
    extensions.configure(MavenPublishBaseExtension::class) {
        publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.S01)

        if (System.getProperty("DisableSigning") != "true") {
            signAllPublications()
        }
        coordinates(extra["base-group"].toString(), this@applyPublishing.name, supabaseVersion)

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
    }
}
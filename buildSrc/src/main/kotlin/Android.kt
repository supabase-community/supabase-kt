import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

fun KotlinMultiplatformExtension.configureLibraryAndroidTarget(
    namespace: String? = null,
    minSdk: Int = 23,
    javaVersion: JvmTarget = JvmTarget.JVM_1_8
) {
    android {
        this.namespace = namespace ?: "${project.extra["base-group"].toString().replace("-", ".")}.${this@configureLibraryAndroidTarget.project.name.replace("-", "")}.library"
        compileSdk = 37
        this.minSdk = minSdk
        compilerOptions {
            jvmTarget.set(javaVersion)
        }
        lint {
            abortOnError = false
        }
    }
    /*extensions.configure(LibraryExtension::class) {
        compileSdk = 37
        sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
        this.namespace =
        defaultConfig {
            this.minSdk = minSdk
        }
        lint {
            abortOnError = false
        }
        compileOptions {
            sourceCompatibility = javaVersion
            targetCompatibility = javaVersion
        }
    }*/

}

fun Project.configureApplicationAndroidTarget(
    javaVersion: JavaVersion = JavaVersion.VERSION_1_8
) {
    (this as org.gradle.api.plugins.ExtensionAware).extensions.configure<ApplicationExtension>("android") {
        compileSdk = 37
        defaultConfig {
            applicationId = "io.github.jan.supabase.android"
            minSdk = 26
            versionCode = 1
            versionName = "1.0-SNAPSHOT"
        }
        namespace = "io.github.jan.supabase.android"
        compileOptions {
            sourceCompatibility = javaVersion
            targetCompatibility = javaVersion
        }
        buildTypes {
            getByName("release") {
                isMinifyEnabled = false
            }
        }
    }
}
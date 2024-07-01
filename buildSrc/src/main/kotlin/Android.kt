import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.get

fun Project.configureLibraryAndroidTarget(
    namespace: String? = null,
    minSdk: Int = 21,
    javaVersion: JavaVersion = JavaVersion.VERSION_1_8
) {
    extensions.configure(LibraryExtension::class) {
        compileSdk = 34
        sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
        this.namespace = namespace ?: "${extra["base-group"].toString().replace("-", ".")}.${this@configureLibraryAndroidTarget.name.replace("-", "")}.library"
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
    }
}

fun Project.configureApplicationAndroidTarget() {
    extensions.configure(BaseAppModuleExtension::class) {
        compileSdk = 34
        defaultConfig {
            applicationId = "io.github.jan.supabase.android"
            minSdk = 26
            versionCode = 1
            versionName = "1.0-SNAPSHOT"
        }
        namespace = "io.github.jan.supabase.android"
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
        }
        buildTypes {
            getByName("release") {
                isMinifyEnabled = false
            }
        }
        /*kotlinOptions {
            jvmTarget = "11"
        }*/
    }
}
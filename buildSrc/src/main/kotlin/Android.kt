import com.android.build.gradle.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.get

fun Project.configureAndroidTarget(module: String) {
    extensions.configure(LibraryExtension::class) {
        compileSdk = 34
        sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
        namespace = "${extra["base-group"]}.$module.library"
        defaultConfig {
            minSdk = 21
        }
        lint {
            abortOnError = false
        }
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
        }
    }
}
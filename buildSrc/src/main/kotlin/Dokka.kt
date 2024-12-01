import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import java.net.URL

fun Project.applyDokkaWithConfiguration() {
    tasks.withType<org.jetbrains.dokka.gradle.DokkaTaskPartial>().configureEach {
        dokkaSourceSets.configureEach {
            sourceLink {
                val name = when(moduleName.get()) {
                    "supabase-kt" -> "Supabase"
                    "functions-kt" -> "Functions"
                    "auth-kt" -> "Auth"
                    "postgrest-kt" -> "Postgrest"
                    "realtime-kt" -> "Realtime"
                    "storage-kt" -> "Storage"
                    "apollo-graphql" -> "plugins/ApolloGraphQL"
                    "compose-auth" -> "plugins/ComposeAuth"
                    "compose-auth-ui" -> "plugins/ComposeAuthUI"
                    "coil-integration" -> "plugins/CoilIntegration"
                    "coil3-integration" -> "plugins/Coil3Integration"
                    "imageloader-integration" -> "plugins/ImageLoaderIntegration"
                    "serializer-moshi" -> "serializers/Moshi"
                    "serializer-jackson" -> "serializers/Jackson"
                    else -> ""
                }
                localDirectory.set(projectDir.resolve("src"))
                remoteUrl.set(URL("https://github.com/supabase/supabase-kt/tree/master/$name/src"))
                remoteLineSuffix.set("#L")
            }
        }
    }
}
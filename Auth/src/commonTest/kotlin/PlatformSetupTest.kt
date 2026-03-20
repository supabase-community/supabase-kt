import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.minimalConfig
import io.github.jan.supabase.testing.createMockedSupabaseClient

internal suspend fun createMinimalAuthClient(
    autoSetup: Boolean,
    authHandler: suspend (Auth) -> Unit
) {
    val client = createMockedSupabaseClient(
        configuration = {
            install(Auth) {
                autoSetupPlatform = autoSetup
                minimalConfig()
            }
        }
    )
    try {
        authHandler(client.auth)
    } finally {
        client.close()
    }
}

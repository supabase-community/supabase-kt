import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.testing.createMockedSupabaseClient

internal fun createMinimalAuthClient(
    autoSetup: Boolean,
) = createMockedSupabaseClient(
    configuration = {
        install(Auth) {
            autoSetupPlatform = autoSetup
            autoLoadFromStorage = false
        }
    }
).auth

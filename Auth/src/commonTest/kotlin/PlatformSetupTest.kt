import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.testing.createMockedSupabaseClient
import kotlinx.coroutines.CoroutineDispatcher

internal fun createAuthClientOld(
    autoSetup: Boolean,
    dispatcher: CoroutineDispatcher
) = createMockedSupabaseClient(
    configuration = {
        coroutineDispatcher = dispatcher
        install(Auth) {
            autoSetupPlatform = autoSetup
            autoLoadFromStorage = false
        }
    }
).auth

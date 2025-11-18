import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.MemorySessionManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.testing.createMockedSupabaseClient

internal fun createAuthClient(
    autoLoad: Boolean,
    sessionFound: Boolean
) = createMockedSupabaseClient(
    configuration = {
        install(Auth) {
            autoSetupPlatform
            autoLoadFromStorage = autoLoad
            sessionManager = if(sessionFound) MemorySessionManager(UserSession("token", "token", expiresIn = 1000, tokenType = "Bearer")) else MemorySessionManager()
        }
    }
).auth

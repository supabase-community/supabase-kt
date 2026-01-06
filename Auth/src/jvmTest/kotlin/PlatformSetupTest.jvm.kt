import io.github.jan.supabase.auth.setupPlatform
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.test.runTest
import kotlin.test.assertIs

class PlatformSetupTest {

    @kotlin.test.Test
    fun testPlatformSetupTestNoAutoLoad() = runTest {
        val auth = createMinimalAuthClient(autoSetup = false)
        auth.setupPlatform()
        assertIs<SessionStatus.NotAuthenticated>(auth.sessionStatus.value)
    }

}
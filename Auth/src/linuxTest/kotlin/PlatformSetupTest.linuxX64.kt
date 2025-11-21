import io.github.jan.supabase.auth.AuthImpl
import io.github.jan.supabase.auth.setupPlatform
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.test.runTest
import kotlin.test.assertEquals
import kotlin.test.assertIs

actual class PlatformSetupTest {

    @kotlin.test.Test
    actual fun testPlatformSetupTestNoAutoLoad() {
        val auth = createAuthClientOld(autoSetup = false, sessionFound = false)
        auth.setupPlatform()
        assertIs<SessionStatus.NotAuthenticated>(auth.sessionStatus.value)
    }

    @kotlin.test.Test
    actual fun testPlatformSetupTestAutoLoad() {
        runTest {
            val auth = createAuthClientOld(autoSetup = true, sessionFound = false) as AuthImpl
            assertEquals(SessionStatus.Initializing, auth.sessionStatus.value)
            auth.setupPlatform()
            assertIs<SessionStatus.NotAuthenticated>(auth.sessionStatus.value)
        }
    }

}
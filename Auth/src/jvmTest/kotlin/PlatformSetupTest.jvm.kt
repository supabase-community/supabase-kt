import io.github.jan.supabase.auth.AuthImpl
import io.github.jan.supabase.auth.setupPlatform
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.test.runTest
import kotlin.test.assertEquals
import kotlin.test.assertIs

class PlatformSetupTest {

    @kotlin.test.Test
    fun testPlatformSetupTestNoAutoLoad() {
        val auth = createAuthClient(autoLoad = false, sessionFound = false)
        auth.setupPlatform()
        assertIs<SessionStatus.NotAuthenticated>(auth.sessionStatus.value)
    }

    @kotlin.test.Test
    fun testPlatformSetupTestAutoLoad() {
        runTest {
            val auth = createAuthClient(autoLoad = true, sessionFound = false) as AuthImpl
            assertEquals(SessionStatus.Initializing, auth.sessionStatus.value)
            auth.setupPlatform()
            assertIs<SessionStatus.NotAuthenticated>(auth.sessionStatus.value)
        }
    }

}
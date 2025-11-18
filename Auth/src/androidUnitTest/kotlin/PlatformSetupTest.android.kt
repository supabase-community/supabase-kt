import io.github.jan.supabase.auth.AuthImpl
import io.github.jan.supabase.auth.setupPlatform
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.job
import kotlinx.coroutines.test.runTest
import kotlin.test.assertEquals
import kotlin.test.assertIs

actual class PlatformSetupTest {

    @kotlin.test.Test
    actual fun testPlatformSetupTestNoAutoLoad() {
        val auth = createAuthClient(autoLoad = false, sessionFound = false)
        assertEquals(SessionStatus.Initializing, auth.sessionStatus.value)
        auth.setupPlatform()
        assertIs<SessionStatus.NotAuthenticated>(auth.sessionStatus.value)
    }

    @kotlin.test.Test
    actual fun testPlatformSetupTestAutoLoad() {
        runTest {
            val auth = createAuthClient(autoLoad = true, sessionFound = true) as AuthImpl
            auth.authScope.coroutineContext.job.join()
            assertIs<SessionStatus.Authenticated>(auth.sessionStatus.value)
            auth.setupPlatform()
            assertIs<SessionStatus.Authenticated>(auth.sessionStatus.value)
        }
    }

}
import io.github.jan.supabase.auth.AuthImpl
import io.github.jan.supabase.auth.setupPlatform
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.job
import kotlinx.coroutines.test.runTest
import kotlin.test.assertEquals
import kotlin.test.assertIs

class PlatformSetupTest {

    @kotlin.test.Test
    fun testPlatformSetupTestNoAutoLoad() = runTest {
        val auth = createMinimalAuthClient(autoSetup = false)
        assertEquals(SessionStatus.Initializing, auth.sessionStatus.value)
        auth.setupPlatform()
        assertIs<SessionStatus.NotAuthenticated>(auth.sessionStatus.value)
    }

    @kotlin.test.Test
    fun testPlatformSetupTestAutoLoad() = runTest {
        val auth = createMinimalAuthClient(autoSetup = true) as AuthImpl
        auth.authScope.coroutineContext.job.join()
        assertIs<SessionStatus.Authenticated>(auth.sessionStatus.value)
        auth.setupPlatform()
        assertIs<SessionStatus.Authenticated>(auth.sessionStatus.value)
    }

}
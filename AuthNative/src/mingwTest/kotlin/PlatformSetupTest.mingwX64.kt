import io.github.jan.supabase.auth.native.setupNativePlatform
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.test.runTest
import kotlin.test.assertEquals
import kotlin.test.assertIs

class PlatformSetupTest {

    @kotlin.test.Test
    fun testPlatformSetupTestNoAutoLoad() = runTest {
        val auth = createMinimalAuthClient(autoSetup = false) {
            it.setupNativePlatform()
            assertIs<SessionStatus.NotAuthenticated>(it.sessionStatus.value)
        }
    }

    @kotlin.test.Test
    fun testPlatformSetupTestAutoLoad() =  runTest {
        val auth = createMinimalAuthClient(autoSetup = true) {
            assertEquals(SessionStatus.Initializing, it.sessionStatus.value)
            it.setupNativePlatform()
            assertIs<SessionStatus.NotAuthenticated>(it.sessionStatus.value)
        }
    }

}
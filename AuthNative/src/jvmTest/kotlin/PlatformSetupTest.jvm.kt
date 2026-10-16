import io.github.jan.supabase.auth.native.setupNativePlatform
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertIs

class PlatformSetupTest {

    @Test
    fun testPlatformSetupTestNoAutoLoad() = runTest {
        createMinimalAuthClient(autoSetup = false) {
            it.setupNativePlatform()
            assertIs<SessionStatus.NotAuthenticated>(it.sessionStatus.value)
        }
    }

}
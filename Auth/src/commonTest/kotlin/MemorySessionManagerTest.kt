import io.github.jan.supabase.gotrue.MemorySessionManager
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class MemorySessionManagerTest {

    @Test
    fun testMemorySessionManager() {
        runTest {
            val session = userSession()
            val sessionManager = MemorySessionManager(session)
            assertEquals(session, sessionManager.loadSession()) //Check if the session is loaded correctly
            val newSession = userSession(200)
            sessionManager.saveSession(newSession)
            assertEquals(newSession, sessionManager.loadSession()) //Check if the new session is saved correctly
            assertNotEquals(session, sessionManager.loadSession()) //Check if the new session is different from the old session
            sessionManager.deleteSession()
            assertNull(sessionManager.loadSession()) //Check if the session is deleted correctly
        }
    }

}
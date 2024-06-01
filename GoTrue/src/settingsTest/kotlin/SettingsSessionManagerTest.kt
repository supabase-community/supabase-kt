import com.russhwolf.settings.MapSettings
import io.github.jan.supabase.gotrue.SettingsSessionManager
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class SettingsSessionManagerTest {

    @Test
    fun testSettingsSessionManager() {
        runTest {
            val session = userSession()
            val sessionManager = SettingsSessionManager(MapSettings(SettingsSessionManager.SETTINGS_KEY to Json.encodeToString(session)))
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
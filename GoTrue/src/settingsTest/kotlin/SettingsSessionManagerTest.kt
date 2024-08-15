import com.russhwolf.settings.MapSettings
import io.github.jan.supabase.gotrue.SettingsSessionManager
import io.github.jan.supabase.gotrue.user.UserSession
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class SettingsSessionManagerTest {

    @Test
    fun testSettingsSessionManager() {
        val json = Json {
            encodeDefaults = true
        }
        runTest {
            val session = userSession()
            val settings = MapSettings(SettingsSessionManager.SETTINGS_KEY to json.encodeToString(session))
            val sessionManager = SettingsSessionManager(settings)
            assertEquals(session, sessionManager.loadSession()) //Check if the session is loaded correctly
            assertEquals(session, json.decodeFromString(settings.getString(SettingsSessionManager.SETTINGS_KEY, ""))) //Check if the session is saved correctly
            val newSession = userSession(expiresIn = 200)
            sessionManager.saveSession(newSession)
            assertEquals(newSession, sessionManager.loadSession()) //Check if the new session is saved correctly
            assertEquals(newSession,
                json.decodeFromString<UserSession>(settings.getString(SettingsSessionManager.SETTINGS_KEY, ""))
            ) //Check if the new session is saved correctly
            assertNotEquals(session, sessionManager.loadSession()) //Check if the new session is different from the old session
            sessionManager.deleteSession()
            assertNull(sessionManager.loadSession()) //Check if the session is deleted correctly
            assertFalse(settings.hasKey(SettingsSessionManager.SETTINGS_KEY)) //Check if the session is deleted correctly
        }
    }

}
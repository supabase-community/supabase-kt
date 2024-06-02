import kotlin.test.Test

class SettingsSessionManagerTest {

    @Test
    fun testSettingsSessionManager() {
        /*val expiresAt = Clock.System.now() + 3600.seconds
        runTest {
            val session = userSession().copy(expiresAt = expiresAt)
            val settings = MapSettings(SettingsSessionManager.SETTINGS_KEY to Json.encodeToString(session))
            val sessionManager = SettingsSessionManager(settings)
            assertEquals(session, sessionManager.loadSession()?.copy(expiresAt = expiresAt)) //Check if the session is loaded correctly
            assertEquals(session, Json.decodeFromString(settings.getString(SettingsSessionManager.SETTINGS_KEY, ""))) //Check if the session is saved correctly
            val newSession = userSession(200).copy(expiresAt = expiresAt)
            sessionManager.saveSession(newSession)
            assertEquals(newSession, sessionManager.loadSession()?.copy(expiresAt = expiresAt)) //Check if the new session is saved correctly
            assertEquals(newSession,
                Json.decodeFromString<UserSession>(settings.getString(SettingsSessionManager.SETTINGS_KEY, ""))
                    .copy(expiresAt = expiresAt)
            ) //Check if the new session is saved correctly
            assertNotEquals(session, sessionManager.loadSession()?.copy(expiresAt = expiresAt)) //Check if the new session is different from the old session
            sessionManager.deleteSession()
            assertNull(sessionManager.loadSession()) //Check if the session is deleted correctly
            assertFalse(settings.hasKey(SettingsSessionManager.SETTINGS_KEY)) //Check if the session is deleted correctly
        }*/
    }

}
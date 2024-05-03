import io.github.jan.supabase.SupabaseClientBuilder
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.MemorySessionManager
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.minimalSettings
import io.github.jan.supabase.gotrue.user.UserSession
import io.github.jan.supabase.testing.createMockedSupabaseClient
import io.github.jan.supabase.testing.respondJson
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class AuthTest {

    private val configuration: SupabaseClientBuilder.() -> Unit = {
        install(Auth) {
            minimalSettings()
        }
    }

    @Test
    fun testLoadingSessionFromStorage() {
        runTest {
            val sessionManager = MemorySessionManager(userSession())
            val client = createMockedSupabaseClient(
                configuration = {
                    install(Auth) {
                        minimalSettings(
                            sessionManager = sessionManager,
                            autoLoadFromStorage = true
                        )
                    }
                }
            )
            client.auth.awaitInitialization()
            assertIs<SessionStatus.Authenticated>(client.auth.sessionStatus.value)
        }
    }

    @Test
    fun testSavingSessionToStorage() {
        runTest {
            val sessionManager = MemorySessionManager()
            val client = createMockedSupabaseClient(
                configuration = {
                    install(Auth) {
                        minimalSettings(
                            sessionManager = sessionManager,
                            autoSaveToStorage = true
                        )
                    }
                }
            )
            client.auth.awaitInitialization()
            assertIs<SessionStatus.NotAuthenticated>(client.auth.sessionStatus.value)
            assertNull(sessionManager.loadSession())
            val session = userSession()
            client.auth.importSession(session)
            assertIs<SessionStatus.Authenticated>(client.auth.sessionStatus.value)
            assertEquals(session, sessionManager.loadSession())
        }
    }

    @Test
    fun testImportExpiredSession() {
        runTest {
            val newSession = userSession()
            val client = createMockedSupabaseClient(configuration = configuration) {
                respondJson(newSession)
            }
            client.auth.awaitInitialization()
            assertIs<SessionStatus.NotAuthenticated>(client.auth.sessionStatus.value)
            val session = userSession(0)
            client.auth.importSession(session)
            assertIs<SessionStatus.Authenticated>(client.auth.sessionStatus.value)
            assertEquals(newSession.expiresIn, client.auth.currentSessionOrNull()?.expiresIn)
        }
    }

    private fun userSession(expiresIn: Long = 3600) = UserSession(
        accessToken = "accessToken",
        refreshToken = "refreshToken",
        expiresIn = expiresIn,
        tokenType = "Bearer"
    )

}
import io.github.jan.supabase.SupabaseClientBuilder
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.MemorySessionManager
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.minimalSettings
import io.github.jan.supabase.gotrue.providers.Github
import io.github.jan.supabase.gotrue.user.UserSession
import io.github.jan.supabase.testing.createMockedSupabaseClient
import io.github.jan.supabase.testing.pathAfterVersion
import io.github.jan.supabase.testing.respondJson
import io.ktor.http.Url
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.seconds

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
            val client = createMockedSupabaseClient(configuration = {
                install(Auth) {
                    minimalSettings(
                        alwaysAutoRefresh = true
                    )
                }
            }) {
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

    @Test
    fun testAutoRefreshSession() {
        runTest {
            val newSession = userSession()
            val client = createMockedSupabaseClient(configuration = {
                install(Auth) {
                    minimalSettings(
                        autoLoadFromStorage = false,
                        alwaysAutoRefresh = false,
                        autoSaveToStorage = false
                    )
                }
            }) {
                respondJson(newSession)
            }
            client.auth.awaitInitialization()
            assertIs<SessionStatus.NotAuthenticated>(client.auth.sessionStatus.value)
            val session = userSession(0)
            client.auth.importSession(session)
            assertIs<SessionStatus.Authenticated>(client.auth.sessionStatus.value)
            assertEquals(session.expiresIn, client.auth.currentSessionOrNull()?.expiresIn) //The session shouldn't be refreshed automatically as alwaysAutoRefresh is false
            client.auth.startAutoRefreshForCurrentSession()
            assertEquals(newSession.expiresIn, client.auth.currentSessionOrNull()?.expiresIn)
        }
    }

    @Test
    fun testGetOAuthUrl() {
        runTest {
            val expectedProvider = Github
            val expectedRedirectUrl = "https://example.com/auth/callback"
            val endpoint = "authorize/custom/endpoint"
            val supabaseUrl = "https://id.supabase.co"
            val client = createMockedSupabaseClient(supabaseUrl = supabaseUrl, configuration = configuration)
            client.auth.awaitInitialization()
            val url = Url(client.auth.getOAuthUrl(expectedProvider, expectedRedirectUrl, endpoint) {
                queryParams["key"] = "value"
                scopes.add("scope1")
                scopes.add("scope2")
            })
            assertEquals(
                endpoint,
                url.pathAfterVersion().substring(1)
            )
            assertEquals(
                expectedProvider.name,
                url.parameters["provider"]
            )
            assertEquals(
                expectedRedirectUrl,
                url.parameters["redirect_to"]
            )
            assertEquals(
                "value",
                url.parameters["key"]
            )
            assertEquals(
                "scope1 scope2",
                url.parameters["scopes"]
            )
        }

    }

}

fun userSession(expiresIn: Long = 3600) = UserSession(
    accessToken = "accessToken",
    refreshToken = "refreshToken",
    expiresIn = expiresIn,
    tokenType = "Bearer"
)
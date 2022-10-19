@file:OptIn(ExperimentalCoroutinesApi::class)

import com.russhwolf.settings.MapSettings
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.exceptions.UnauthorizedException
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.gotrue.GoTrueImpl
import io.github.jan.supabase.gotrue.SettingsSessionManager
import io.github.jan.supabase.gotrue.currentAccessToken
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.user.UserInfo
import io.github.jan.supabase.gotrue.user.UserSession
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class GoTrueTest {

    val mockEngine = GoTrueMock().engine
    @OptIn(ExperimentalCoroutinesApi::class)
    val dispatcher = StandardTestDispatcher()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun test_login_with_email_with_wrong_credentials() {
        val client = createSupabaseClient()
        runTest(dispatcher) {
            assertFailsWith<RestException>("Login with email and wrong password should fail") {
                client.gotrue.loginWith(Email, "https://website.com") {
                    email = "email@example.com"
                    password = "wrong_password"
                }
            }
            client.close()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun test_login_with_email_with_correct_credentials() {
        val client = createSupabaseClient()
        runTest(dispatcher) {
            client.gotrue.loginWith(Email) {
                email = "email@example.com"
                password = GoTrueMock.VALID_PASSWORD
            }
            assertEquals(GoTrueMock.NEW_ACCESS_TOKEN, client.gotrue.currentAccessToken())
            client.close()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun test_sign_up_with_email() {
        val client = createSupabaseClient()
        runTest(dispatcher) {
            val result = client.gotrue.signUpWith(Email) {
                email = "email@example.com"
                password = GoTrueMock.VALID_PASSWORD
            }
            assertEquals("email@example.com", result.email)
            client.close()
        }
    }

    @Test
    fun test_import_jwt_token() {
        val client = createSupabaseClient()
        runTest(dispatcher) {
            client.gotrue.importAuthToken("some_token")
            assertEquals("some_token", client.gotrue.currentAccessToken())
            client.close()
        }
    }

    @Test
    fun test_import_session_and_invalidate_session() {
        val client = createSupabaseClient()
        runTest(dispatcher) {
            val session = UserSession("some_token", "some_refresh_token", 20, "token_type", null)
            client.gotrue.importSession(session, false)
            assertEquals("some_token", client.gotrue.currentAccessToken())
            assertEquals("some_refresh_token", client.gotrue.currentSession.value!!.refreshToken)
            client.gotrue.invalidateSession()
            assertEquals(null, client.gotrue.currentAccessToken())
            client.close()
        }
    }

    @Test
    fun test_auto_refresh_with_wrong_token() {
        val client = createSupabaseClient {
            alwaysAutoRefresh = true
        }
        runTest(dispatcher) {
            val session = UserSession("old_token", "some_refresh_token", 0, "token_type", null)
            client.gotrue.importSession(session, true)
            assertNull(client.gotrue.currentAccessToken(), null)
            client.close()
        }
    }

    @Test
    fun test_auto_refresh_with_correct_token() {
        val client = createSupabaseClient()
        runTest(dispatcher) {
            val session = UserSession("old_token", GoTrueMock.VALID_REFRESH_TOKEN, 0, "token_type", null)
            client.gotrue.importSession(session, true)
            assertEquals(GoTrueMock.NEW_ACCESS_TOKEN, client.gotrue.currentAccessToken())
            client.close()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun test_loading_session_from_storage() {
        val client = createSupabaseClient {
            sessionManager = SettingsSessionManager(MapSettings(
                "session" to Json.encodeToString(UserSession("token", "refresh_token", 1000, "type", null))
            ))
        }
        runTest {
            client.gotrue.loadFromStorage()
            assertNotNull(client.gotrue.currentSession.value)
            client.close()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun test_requesting_user_with_invalid_token() {
        val client = createSupabaseClient()
        runTest(dispatcher) {
            assertFailsWith<UnauthorizedException>("Requesting user with invalid token should fail") {
                client.gotrue.getUser("invalid_token")
            }
            client.close()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun test_requesting_user_with_valid_token() {
        val client = createSupabaseClient()
        runTest(dispatcher) {
            val user = client.gotrue.getUser(GoTrueMock.VALID_ACCESS_TOKEN)
            assertEquals("userid", user.id)
            client.close()
        }
    }

    private fun createSupabaseClient(additionalGoTrueSettings: GoTrue.Config.() -> Unit = {}): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = "https://example.com",
            supabaseKey = "example",
        ) {
            httpEngine = mockEngine

            install(GoTrue) {
                autoLoadFromStorage = false
                alwaysAutoRefresh = false
                coroutineDispatcher = dispatcher

                sessionManager = SettingsSessionManager(MapSettings())

                additionalGoTrueSettings()
            }
        }
    }

}
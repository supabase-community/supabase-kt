import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.exceptions.BadRequestRestException
import io.github.jan.supabase.exceptions.UnauthorizedRestException
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.AuthConfig
import io.github.jan.supabase.gotrue.MemoryCodeVerifierCache
import io.github.jan.supabase.gotrue.MemorySessionManager
import io.github.jan.supabase.gotrue.OtpType
import io.github.jan.supabase.gotrue.SessionSource
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.Github
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.providers.builtin.OTP
import io.github.jan.supabase.gotrue.user.UserSession
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GoTrueTest {

    private val mockEngine = GoTrueMock().engine

    private val dispatcher = StandardTestDispatcher()

    @Test
    fun test_login_with_email_with_wrong_credentials() {
        val client = createSupabaseClient()
        runTest(dispatcher) {
            assertFailsWith<BadRequestRestException>("Login with email and wrong password should fail") {
                client.auth.signInWith(Email, "https://website.com") {
                    email = "email@example.com"
                    password = "wrong_password"
                }
            }
            client.close()
        }
    }

    @Test
    fun test_login_with_email_with_correct_credentials() {
        val client = createSupabaseClient()
        runTest(dispatcher) {
            client.auth.signInWith(Email) {
                email = "email@example.com"
                password = GoTrueMock.VALID_PASSWORD
            }
            assertEquals(GoTrueMock.NEW_ACCESS_TOKEN, client.auth.currentAccessTokenOrNull())
            assertIs<SessionSource.SignIn>(client.auth.sessionSource())
            assertIs<Email>(client.auth.sessionSourceAs<SessionSource.SignIn>().provider)
            client.close()
        }
    }

    @Test
    fun test_sign_up_with_email() {
        val client = createSupabaseClient()
        runTest(dispatcher) {
            val result = client.auth.signUpWith(Email) {
                email = "email@example.com"
                password = GoTrueMock.VALID_PASSWORD
            } ?: error("Sign up with email should not return null")
            assertEquals("email@example.com", result.email)
            client.close()
        }
    }

    @Test
    fun test_import_jwt_token() {
        val client = createSupabaseClient()
        runTest(dispatcher) {
            client.auth.importAuthToken("some_token")
            assertEquals("some_token", client.auth.currentAccessTokenOrNull())
            client.close()
        }
    }

    @Test
    fun test_import_session_and_invalidate_session() {
        val client = createSupabaseClient()
        runTest(dispatcher) {
            val session =
                UserSession("some_token", "some_refresh_token", "", "", 20, "token_type", null)
            client.auth.importSession(session, false, source = SessionSource.External)
            assertIs<SessionSource.External>(client.auth.sessionSource())
            assertEquals("some_token", client.auth.currentAccessTokenOrNull())
            assertEquals("some_refresh_token", client.auth.currentSessionOrNull()!!.refreshToken)
            client.auth.signOut()
            assertNull(client.auth.currentAccessTokenOrNull())
            assertIs<SessionStatus.NotAuthenticated>(client.auth.sessionStatus.value)
            assertTrue {
                (client.auth.sessionStatus.value as SessionStatus.NotAuthenticated).isSignOut
            }
            client.close()
        }
    }

    @Test
    fun test_auto_refresh_with_wrong_token() {
        val client = createSupabaseClient {
            alwaysAutoRefresh = true
        }
        runTest(dispatcher) {
            val session =
                UserSession("old_token", "some_refresh_token", "", "", 0, "token_type", null)
            client.auth.importSession(session, true)
            assertNull(client.auth.currentAccessTokenOrNull(), null)
            client.close()
        }
    }

    @Test
    fun test_auto_refresh_with_correct_token() {
        val client = createSupabaseClient()
        runTest(dispatcher) {
            val session = UserSession(
                "old_token",
                GoTrueMock.VALID_REFRESH_TOKEN,
                "",
                "",
                0,
                "token_type",
                null
            )
            client.auth.importSession(session, true)
            assertEquals(GoTrueMock.NEW_ACCESS_TOKEN, client.auth.currentAccessTokenOrNull())
            assertIs<SessionSource.Refresh>(client.auth.sessionSource())
            client.close()
        }
    }

    @Test
    fun test_loading_session_from_storage() {
        val client = createSupabaseClient {
            sessionManager = MemorySessionManager(
                UserSession(
                    "token",
                    "refresh_token",
                    "",
                    "",
                    1000,
                    "type",
                    null
                )
            )
        }
        runTest {
            assertIs<SessionStatus.NotAuthenticated>(client.auth.sessionStatus.value)
            client.auth.loadFromStorage()
            assertIs<SessionSource.Storage>(client.auth.sessionSource())
            assertNotNull(client.auth.currentSessionOrNull())
            client.close()
        }
    }

    @Test
    fun test_requesting_user_with_invalid_token() {
        val client = createSupabaseClient()
        runTest(dispatcher) {
            assertFailsWith<UnauthorizedRestException>("Requesting user with invalid token should fail") {
                client.auth.retrieveUser("invalid_token")
            }
            client.close()
        }
    }

    @Test
    fun test_requesting_user_with_valid_token() {
        val client = createSupabaseClient()
        runTest(dispatcher) {
            val user = client.auth.retrieveUser(GoTrueMock.VALID_ACCESS_TOKEN)
            assertEquals("userid", user.id)
            client.close()
        }
    }

    @Test
    fun test_verifying_with_wrong_token() {
        val client = createSupabaseClient()
        runTest(dispatcher) {
            assertFailsWith<BadRequestRestException>("verifying with a wrong token should fail") {
                client.auth.verifyEmailOtp(
                    OtpType.Email.INVITE,
                    "example@email.com",
                    "wrong_token"
                )
            }
            client.close()
        }
    }

    @Test
    fun test_verifying_with_valid_token() {
        val client = createSupabaseClient()
        runTest(dispatcher) {
            client.auth.verifyEmailOtp(
                OtpType.Email.INVITE,
                "example@gmail.com",
                GoTrueMock.VALID_VERIFY_TOKEN
            )
            assertEquals(
                GoTrueMock.NEW_ACCESS_TOKEN,
                client.auth.currentAccessTokenOrNull(),
                "verify with valid token should update the user session"
            )
            assertIs<SessionSource.SignIn>(client.auth.sessionSource())
            assertIs<OTP>(client.auth.sessionSourceAs<SessionSource.SignIn>().provider)
        }
    }

    @Test
    fun test_custom_url() {
        val client = createSupabaseClient {
            customUrl = "https://custom.auth.com"
        }
        runTest(dispatcher) {
            assertEquals("https://custom.auth.com/signup", client.auth.resolveUrl("signup"))
            client.close()
        }
    }

    @Test
    fun test_otp_email() {
        val client = createSupabaseClient()
        runTest(dispatcher) {
            client.auth.signInWith(OTP) {
                email = "example@email.com"
            }
            client.close()
        }
    }

    @Test
    fun test_otp_phone() {
        val client = createSupabaseClient()
        runTest(dispatcher) {
            client.auth.signInWith(OTP) {
                phone = "12345678"
            }
            client.close()
        }
    }

    @Test
    fun test_recovery() {
        val client = createSupabaseClient()
        runTest(dispatcher) {
            client.auth.resetPasswordForEmail("example@email.com")
            client.close()
        }
    }

    @Test
    fun test_oauth_url() {
        val client = createSupabaseClient()
        val expected =
            "https://example.com/auth/v1/authorize?provider=github&redirect_to=https://example.com&scopes=test+test2&custom=value"
        val actual = client.auth.getOAuthUrl(Github, "https://example.com") {
            scopes.addAll(listOf("test", "test2"))
            queryParams["custom"] = "value"
        }
        assertEquals(expected, actual)
    }

    private fun createSupabaseClient(additionalGoTrueSettings: AuthConfig.() -> Unit = {}): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = "https://example.com",
            supabaseKey = "example",
        ) {
            httpEngine = mockEngine

            install(Auth) {
                autoLoadFromStorage = false
                alwaysAutoRefresh = false
                coroutineDispatcher = dispatcher

                sessionManager = MemorySessionManager()
                codeVerifierCache = MemoryCodeVerifierCache()

                additionalGoTrueSettings()
                platformSettings()
            }
        }
    }

    private fun Auth.sessionSource() = (sessionStatus.value as SessionStatus.Authenticated).source

    private inline fun <reified T> Auth.sessionSourceAs() = sessionSource() as T

}

expect fun AuthConfig.platformSettings()

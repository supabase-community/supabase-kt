@file:OptIn(ExperimentalCoroutinesApi::class)

import com.russhwolf.settings.MapSettings
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.exceptions.BadRequestRestException
import io.github.jan.supabase.exceptions.UnauthorizedRestException
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.gotrue.GoTrueConfig
import io.github.jan.supabase.gotrue.OtpType
import io.github.jan.supabase.gotrue.SettingsCodeVerifierCache
import io.github.jan.supabase.gotrue.SettingsSessionManager
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.gotrue.providers.Github
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.providers.builtin.Phone
import io.github.jan.supabase.gotrue.user.UserSession
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
            assertFailsWith<BadRequestRestException>("Login with email and wrong password should fail") {
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
            assertEquals(GoTrueMock.NEW_ACCESS_TOKEN, client.gotrue.currentAccessTokenOrNull())
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
            } ?: error("Sign up with email should not return null")
            assertEquals("email@example.com", result.email)
            client.close()
        }
    }

    @Test
    fun test_import_jwt_token() {
        val client = createSupabaseClient()
        runTest(dispatcher) {
            client.gotrue.importAuthToken("some_token")
            assertEquals("some_token", client.gotrue.currentAccessTokenOrNull())
            client.close()
        }
    }

    @Test
    fun test_import_session_and_invalidate_session() {
        val client = createSupabaseClient()
        runTest(dispatcher) {
            val session =
                UserSession("some_token", "some_refresh_token", "", "", 20, "token_type", null)
            client.gotrue.importSession(session, false)
            assertEquals("some_token", client.gotrue.currentAccessTokenOrNull())
            assertEquals("some_refresh_token", client.gotrue.currentSessionOrNull()!!.refreshToken)
            client.gotrue.invalidateSession()
            assertEquals(null, client.gotrue.currentAccessTokenOrNull())
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
            client.gotrue.importSession(session, true)
            assertNull(client.gotrue.currentAccessTokenOrNull(), null)
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
            client.gotrue.importSession(session, true)
            assertEquals(GoTrueMock.NEW_ACCESS_TOKEN, client.gotrue.currentAccessTokenOrNull())
            client.close()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun test_loading_session_from_storage() {
        val client = createSupabaseClient {
            sessionManager = SettingsSessionManager(
                MapSettings(
                    "session" to Json.encodeToString(
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
                )
            )
        }
        runTest {
            client.gotrue.loadFromStorage()
            assertNotNull(client.gotrue.currentSessionOrNull())
            client.close()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun test_requesting_user_with_invalid_token() {
        val client = createSupabaseClient()
        runTest(dispatcher) {
            assertFailsWith<UnauthorizedRestException>("Requesting user with invalid token should fail") {
                client.gotrue.retrieveUser("invalid_token")
            }
            client.close()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun test_requesting_user_with_valid_token() {
        val client = createSupabaseClient()
        runTest(dispatcher) {
            val user = client.gotrue.retrieveUser(GoTrueMock.VALID_ACCESS_TOKEN)
            assertEquals("userid", user.id)
            client.close()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun test_verifying_with_wrong_token() {
        val client = createSupabaseClient()
        runTest(dispatcher) {
            assertFailsWith<BadRequestRestException>("verifying with a wrong token should fail") {
                client.gotrue.verifyEmailOtp(
                    OtpType.Email.INVITE,
                    "example@email.com",
                    "wrong_token"
                )
            }
            client.close()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun test_verifying_with_valid_token() {
        val client = createSupabaseClient()
        runTest(dispatcher) {
            client.gotrue.verifyEmailOtp(
                OtpType.Email.INVITE,
                "example@gmail.com",
                GoTrueMock.VALID_VERIFY_TOKEN
            )
            assertEquals(
                GoTrueMock.NEW_ACCESS_TOKEN,
                client.gotrue.currentAccessTokenOrNull(),
                "verify with valid token should update the user session"
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun test_custom_url() {
        val client = createSupabaseClient {
            customUrl = "https://custom.auth.com"
        }
        runTest(dispatcher) {
            assertEquals("https://custom.auth.com/signup", client.gotrue.resolveUrl("signup"))
            client.close()
        }
    }

    @Test
    fun test_otp_email() {
        val client = createSupabaseClient()
        runTest(dispatcher) {
            client.gotrue.sendOtpTo(Email) {
                email = "example@email.com"
            }
            client.close()
        }
    }

    @Test
    fun test_otp_phone() {
        val client = createSupabaseClient()
        runTest(dispatcher) {
            client.gotrue.sendOtpTo(Phone) {
                phoneNumber = "12345678"
            }
            client.close()
        }
    }

    @Test
    fun test_recovery() {
        val client = createSupabaseClient()
        runTest(dispatcher) {
            client.gotrue.sendRecoveryEmail("example@email.com")
            client.close()
        }
    }

    @Test
    fun test_oauth_url() {
        val client = createSupabaseClient()
        val expected =
            "https://example.com/auth/v1/authorize?provider=github&redirect_to=https://example.com&scopes=test+test2&custom=value"
        val actual = client.gotrue.oAuthUrl(Github, "https://example.com") {
            scopes.addAll(listOf("test", "test2"))
            queryParams["custom"] = "value"
        }
        assertEquals(expected, actual)
    }

    private fun createSupabaseClient(additionalGoTrueSettings: GoTrueConfig.() -> Unit = {}): SupabaseClient {
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
                codeVerifierCache = SettingsCodeVerifierCache(MapSettings())

                platformSettings()
                additionalGoTrueSettings()
            }
        }
    }

}

expect fun GoTrueConfig.platformSettings()
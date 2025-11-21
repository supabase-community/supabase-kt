import app.cash.turbine.test
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.event.AuthEvent
import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.minimalConfig
import io.github.jan.supabase.auth.redirectTo
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.url.checkForUrlParameterError
import io.github.jan.supabase.auth.url.consumeHashParameters
import io.github.jan.supabase.auth.url.consumeUrlParameter
import io.github.jan.supabase.auth.url.getFragmentParts
import io.github.jan.supabase.auth.url.handledUrlParameterError
import io.github.jan.supabase.testing.createMockedSupabaseClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.url
import io.ktor.http.Url
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class UrlUtilsTest {

    @Test
    fun testConsumeHashParameters() {
        val url = "https://example.com/#test=123&state=abc&code=xyz"
        val newUrl = consumeHashParameters(listOf("test", "state"), url)
        val expectedUrl = "https://example.com/#code=xyz"
        assertEquals(expectedUrl, newUrl)
    }

    @Test
    fun testConsumeUrlParameter() {
        val url = "https://example.com/?test=123&state=abc&code=xyz"
        val newUrl = consumeUrlParameter(listOf("test", "state"), url)
        val expectedUrl = "https://example.com/?code=xyz"
        assertEquals(expectedUrl, newUrl)
    }

    @Test
    fun testRedirectTo() {
        val url = "https://example.com/"
        val redirectTo = "https://redirect.com"
        val newUrl = HttpRequestBuilder().apply {
            url(url)
            redirectTo(redirectTo)
        }.url.toString()
        val expectedUrl = "https://example.com/?redirect_to=https%3A%2F%2Fredirect.com"
        assertEquals(expectedUrl, newUrl)
    }

    @Test
    fun testErrorQueryParameter() {
        val url = Url("https://example.com/?error=invalid_request&error_code=otp_expired&error_description=Invalid+request")
        val error = checkForUrlParameterError { url.parameters[it] }
        assertEquals("otp_expired", error?.error)
        assertEquals("Invalid request (invalid_request)", error?.errorDescription)
        assertEquals(AuthErrorCode.OtpExpired, error?.errorCode)
    }

    @Test
    fun testGetFragmentParts() {
        val fragment = "error=invalid_request&error_code=otp_expired&error_description=Invalid+request"
        val parts = getFragmentParts(fragment)
        assertEquals("invalid_request", parts["error"])
        assertEquals("otp_expired", parts["error_code"])
        assertEquals("Invalid+request", parts["error_description"])
    }

    @Test
    fun testHandledUrlParameterErrorWithValidError() {
        val url = Url("https://example.com/?error=invalid_request&error_code=otp_expired&error_description=Invalid+request")
        val supabase = createMockedSupabaseClient(
            configuration = {
                install(Auth) {
                    minimalConfig()
                }
            }
        )
        runTest {
            val handled = supabase.auth.handledUrlParameterError {
                url.parameters[it]
            }
            assertTrue { handled }
            assertIs<SessionStatus.NotAuthenticated>(supabase.auth.sessionStatus.value)
            supabase.auth.events.test {
                val event = expectMostRecentItem()
                assertIs<AuthEvent.OtpError>(event)
                assertEquals(AuthErrorCode.OtpExpired, event.errorCode)
                assertEquals("Invalid request (invalid_request)", event.errorDescription)
            }
        }
    }

    @Test
    fun testHandledUrlParameterErrorWithNoError() {
        val url = Url("https://example.com/")
        val supabase = createMockedSupabaseClient(
            configuration = {
                install(Auth) {
                    minimalConfig()
                }
            }
        )
        runTest {
            val handled = supabase.auth.handledUrlParameterError {
                url.parameters[it]
            }
            assertFalse { handled }
            assertIs<SessionStatus.NotAuthenticated>(supabase.auth.sessionStatus.value)
            supabase.auth.events.test(timeout = 1.seconds) {
                expectNoEvents()
            }
        }
    }

}
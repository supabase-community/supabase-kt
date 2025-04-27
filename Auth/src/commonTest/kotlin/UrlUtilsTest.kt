import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.checkForErrorHash
import io.github.jan.supabase.auth.checkForUrlParameterError
import io.github.jan.supabase.auth.consumeHashParameters
import io.github.jan.supabase.auth.consumeUrlParameter
import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.getFragmentParts
import io.github.jan.supabase.auth.handledUrlParameterError
import io.github.jan.supabase.auth.minimalSettings
import io.github.jan.supabase.auth.redirectTo
import io.github.jan.supabase.auth.status.NotAuthenticatedReason
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.testing.createMockedSupabaseClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.url
import io.ktor.http.Url
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

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
    fun testErrorHash() {
        val url = "error=invalid_request&error_code=otp_expired&error_description=Invalid+request"
        val error = checkForErrorHash(url)
        assertEquals("otp_expired", error?.error)
        assertEquals("Invalid+request (invalid_request)", error?.errorDescription)
        assertEquals(AuthErrorCode.OtpExpired, error?.errorCode)
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
                    minimalSettings()
                }
            }
        )
        val handled = supabase.auth.handledUrlParameterError {
            url.parameters[it]
        }
        assertTrue { handled }
        assertIs<SessionStatus.NotAuthenticated>(supabase.auth.sessionStatus.value)
        val reason = (supabase.auth.sessionStatus.value as SessionStatus.NotAuthenticated).reason
        assertIs<NotAuthenticatedReason.Error>(reason)
        assertEquals(AuthErrorCode.OtpExpired, reason.errorCode)
        assertEquals("Invalid request (invalid_request)", reason.errorDescription)
    }

    @Test
    fun testHandledUrlParameterErrorWithNoError() {
        val url = Url("https://example.com/")
        val supabase = createMockedSupabaseClient(
            configuration = {
                install(Auth) {
                    minimalSettings()
                }
            }
        )
        val handled = supabase.auth.handledUrlParameterError {
            url.parameters[it]
        }
        assertFalse { handled }
        assertIs<SessionStatus.NotAuthenticated>(supabase.auth.sessionStatus.value)
        val reason = (supabase.auth.sessionStatus.value as SessionStatus.NotAuthenticated).reason
        assertIs<NotAuthenticatedReason.SessionNotFound>(reason)
    }

}
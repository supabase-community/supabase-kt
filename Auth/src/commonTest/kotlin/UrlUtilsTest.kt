import io.github.jan.supabase.auth.checkForErrorHash
import io.github.jan.supabase.auth.checkForUrlParameterError
import io.github.jan.supabase.auth.consumeHashParameters
import io.github.jan.supabase.auth.consumeUrlParameter
import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.redirectTo
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.url
import io.ktor.http.Url
import kotlin.test.Test
import kotlin.test.assertEquals

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

}
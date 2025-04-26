import io.github.jan.supabase.auth.consumeHashParameters
import io.github.jan.supabase.auth.consumeUrlParameter
import kotlin.test.Test
import kotlin.test.assertEquals

class UtilsTest {

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

}
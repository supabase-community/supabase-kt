import io.github.jan.supabase.StringMasking.maskHeaders
import io.github.jan.supabase.StringMasking.maskString
import io.github.jan.supabase.StringMasking.maskUrl
import io.ktor.http.Url
import io.ktor.http.headers
import kotlin.test.Test
import kotlin.test.assertEquals

class StringMaskingTest {

    @Test
    fun testEmptyString() {
        assertEquals("", maskString(""))
    }

    @Test
    fun testString() {
        assertEquals("ab...", maskString("abcdefghi"))
    }

    @Test
    fun testStringWithLength() {
        assertEquals("ab... (len=5)", maskString("abcde", showLength = true))
    }

    @Test
    fun testUrl() {
        assertEquals("https://ab.../test?parameter=true", maskUrl(Url("https://abcdefg.supabase.co/test?parameter=true")))
    }

    @Test
    fun testHeaderMasking() {
        val headers = headers {
            set("aa", "bb")
            set("another", "one")
            set("apikey", "areallylongkey")
            set("Authorization", "Bearer thisisasecretkey")
        }
        val maskedString = maskHeaders(headers)
        assertEquals(
            "{aa=[bb], another=[one], apikey=[ar... (len=14)], Authorization=[Bearer th... (len=16)]}",
            maskedString
        )
    }

}
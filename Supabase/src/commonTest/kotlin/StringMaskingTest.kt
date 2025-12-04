import io.github.jan.supabase.maskString
import io.github.jan.supabase.maskUrl
import io.ktor.http.Url
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

}
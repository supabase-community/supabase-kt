package request

import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.postgrest.request.PostgrestRequest
import io.github.jan.supabase.postgrest.request.SelectRequest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SelectRequestTest {

    private lateinit var sut: PostgrestRequest

    @Test
    fun testCreateSelectRequest_isHead_thenReturnCorrectValue() {
        sut = SelectRequest(
            head = true,
            count = Count.EXACT,
            urlParams = mapOf("Key1" to "Value1"),
            schema = "table"
        )
        assertTrue((sut as SelectRequest).head)
        val count = (sut as SelectRequest).count
        assertNotNull(count)
        assertEquals("exact", count.identifier)
        assertEquals("HEAD", sut.method.value)
        assertEquals(
            listOf(
                "count=exact"
            ), sut.prefer
        )
        assertEquals("table", sut.schema)
        assertEquals(mapOf("Key1" to "Value1"), sut.urlParams)
        assertNull(sut.body)
    }

    @Test
    fun testCreateSelectRequest_notHead_thenReturnCorrectValue() {
        sut = SelectRequest(
            head = false,
            count = Count.EXACT,
            urlParams = mapOf("Key1" to "Value1"),
            schema = "table"
        )

        assertFalse((sut as SelectRequest).head)
        val count = (sut as SelectRequest).count
        assertNotNull(count)
        assertEquals("exact", count.identifier)
        assertEquals("GET", sut.method.value)
        assertEquals(
            listOf(
                "count=exact"
            ), sut.prefer
        )
        assertEquals("table", sut.schema)
        assertEquals(mapOf("Key1" to "Value1"), sut.urlParams)
        assertNull(sut.body)
    }

    @Test
    fun testCreateSelectRequest_notHeadAndWithoutCount_thenReturnCorrectValue() {
        sut = SelectRequest(
            head = false,
            count = null,
            urlParams = mapOf("Key1" to "Value1"),
            schema = "table"
        )

        assertFalse((sut as SelectRequest).head)
        val count = (sut as SelectRequest).count
        assertNull(count)

        assertEquals("GET", sut.method.value)
        assertEquals(
            listOf(
            ), sut.prefer
        )
        assertEquals("table", sut.schema)
        assertEquals(mapOf("Key1" to "Value1"), sut.urlParams)
        assertNull(sut.body)
    }

}
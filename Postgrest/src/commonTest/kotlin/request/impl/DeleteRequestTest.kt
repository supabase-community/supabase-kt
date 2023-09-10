package request.impl

import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.postgrest.query.Returning
import io.github.jan.supabase.postgrest.request.PostgrestRequest
import io.github.jan.supabase.postgrest.request.impl.DeleteRequest
import kotlin.test.Test
import kotlin.test.assertEquals

class DeleteRequestTest {

    lateinit var sut: PostgrestRequest

    @Test
    fun testCreateDeleteRequest_thenReturnCorrectValue() {
        sut = DeleteRequest(
            returning = Returning.REPRESENTATION,
            count = Count.EXACT,
            filter = mapOf("Key1" to listOf("Value1")),
            schema = "table"
        )

        assertEquals("DELETE", sut.method.value)
        assertEquals(listOf("return=representation", "count=exact"), sut.prefer)
        assertEquals("table", sut.schema)
        assertEquals(mapOf("Key1" to listOf("Value1")), sut.filter)
    }

    @Test
    fun testCreateDeleteRequestWithoutCount_thenReturnCorrectValue() {
        sut = DeleteRequest(
            returning = Returning.REPRESENTATION,
            count = null,
            filter = mapOf("Key1" to listOf("Value1")),
            schema = "table"
        )

        assertEquals("DELETE", sut.method.value)
        assertEquals(listOf("return=representation"), sut.prefer)
        assertEquals("table", sut.schema)
        assertEquals(mapOf("Key1" to listOf("Value1")), sut.filter)
    }

}
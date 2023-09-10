package request.impl

import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.postgrest.query.Returning
import io.github.jan.supabase.postgrest.request.PostgrestRequest
import io.github.jan.supabase.postgrest.request.impl.UpdateRequest
import kotlinx.serialization.json.JsonArray
import kotlin.test.Test
import kotlin.test.assertEquals

class UpdateRequestTest {

    lateinit var sut: PostgrestRequest


    @Test
    fun testCreateUpdateRequest_thenReturnCorrectValue() {
        sut = UpdateRequest(
            returning = Returning.REPRESENTATION,
            count = Count.EXACT,
            body = JsonArray(listOf()),
            filter = mapOf("Key1" to listOf("Value1")),
            schema = "table"
        )


        assertEquals("PATCH", sut.method.value)
        assertEquals(
            listOf(
                "return=representation",
                "count=exact"
            ), sut.prefer
        )
        assertEquals("table", sut.schema)
        assertEquals(mapOf("Key1" to listOf("Value1")), sut.filter)
        assertEquals(JsonArray(listOf()), sut.body)
        assertEquals(mapOf(), sut.urlParams)
    }

    @Test
    fun testCreateUpdateRequestWithoutCount_thenReturnCorrectValue() {
        sut = UpdateRequest(
            returning = Returning.REPRESENTATION,
            count = null,
            body = JsonArray(listOf()),
            filter = mapOf("Key1" to listOf("Value1")),
            schema = "table"
        )

        assertEquals("PATCH", sut.method.value)
        assertEquals(
            listOf(
                "return=representation",
            ), sut.prefer
        )
        assertEquals("table", sut.schema)
        assertEquals(mapOf("Key1" to listOf("Value1")), sut.filter)
        assertEquals(JsonArray(listOf()), sut.body)
        assertEquals(mapOf(), sut.urlParams)
    }

}
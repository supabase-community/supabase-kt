package request

import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.postgrest.query.Returning
import io.github.jan.supabase.postgrest.request.PostgrestRequest
import io.github.jan.supabase.postgrest.request.UpdateRequest
import kotlinx.serialization.json.JsonArray
import kotlin.test.Test
import kotlin.test.assertEquals

class UpdateRequestTest {

    private lateinit var sut: PostgrestRequest


    @Test
    fun testCreateUpdateRequest_thenReturnCorrectValue() {
        sut = UpdateRequest(
            returning = Returning.Representation(),
            count = Count.EXACT,
            body = JsonArray(listOf()),
            urlParams = mapOf("Key1" to "Value1"),
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
        assertEquals(mapOf("Key1" to "Value1"), sut.urlParams)
        assertEquals(JsonArray(listOf()), sut.body)
    }

    @Test
    fun testCreateUpdateRequest_withoutCount_thenReturnCorrectValue() {
        sut = UpdateRequest(
            returning = Returning.Representation(),
            count = null,
            body = JsonArray(listOf()),
            urlParams = mapOf("Key1" to "Value1"),
            schema = "table"
        )

        assertEquals("PATCH", sut.method.value)
        assertEquals(
            listOf(
                "return=representation",
            ), sut.prefer
        )
        assertEquals("table", sut.schema)
        assertEquals(mapOf("Key1" to "Value1"), sut.urlParams)
        assertEquals(JsonArray(listOf()), sut.body)
    }

}
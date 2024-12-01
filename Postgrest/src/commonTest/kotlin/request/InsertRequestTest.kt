package request

import io.supabase.postgrest.query.Count
import io.supabase.postgrest.query.Returning
import io.supabase.postgrest.request.InsertRequest
import io.supabase.postgrest.request.PostgrestRequest
import kotlinx.serialization.json.JsonArray
import kotlin.test.Test
import kotlin.test.assertEquals

class InsertRequestTest {

    private lateinit var sut: PostgrestRequest

    @Test
    fun testCreateInsertRequest_withUpsert_thenReturnCorrectValue() {
        sut = InsertRequest(
            returning = Returning.Representation(),
            count = Count.EXACT,
            upsert = true,
            body = JsonArray(listOf()),
            urlParams = mapOf("Key1" to "Value1"),
            schema = "table"
        )

        assertEquals("POST", sut.method.value)
        assertEquals(
            listOf(
                "return=representation",
                "resolution=merge-duplicates",
                "missing=default",
                "count=exact"
            ), sut.prefer
        )
        assertEquals("table", sut.schema)
        assertEquals(mapOf("Key1" to "Value1"), sut.urlParams)
        assertEquals(JsonArray(listOf()), sut.body)
    }

    @Test
    fun testCreateInsertRequest_notUpsert_thenReturnCorrectValue() {
        sut = InsertRequest(
            returning = Returning.Representation(),
            count = Count.EXACT,
            upsert = false,
            body = JsonArray(listOf()),
            urlParams = mapOf("Key1" to "Value1"),
            schema = "table"
        )

        assertEquals("POST", sut.method.value)
        assertEquals(
            listOf(
                "return=representation",
                "missing=default",
                "count=exact"
            ), sut.prefer
        )
        assertEquals("table", sut.schema)
        assertEquals(mapOf("Key1" to "Value1"), sut.urlParams)
        assertEquals(JsonArray(listOf()), sut.body)
    }

    @Test
    fun testCreateInsertRequest_withoutCount_thenReturnCorrectValue() {
        sut = InsertRequest(
            returning = Returning.Representation(),
            count = null,
            upsert = false,
            body = JsonArray(listOf()),
            urlParams = mapOf("Key1" to "Value1"),
            schema = "table"
        )

        assertEquals("POST", sut.method.value)
        assertEquals(
            listOf(
                "return=representation",
                "missing=default"
            ), sut.prefer
        )
        assertEquals("table", sut.schema)
        assertEquals(mapOf("Key1" to "Value1"), sut.urlParams)
        assertEquals(JsonArray(listOf()), sut.body)
    }

}
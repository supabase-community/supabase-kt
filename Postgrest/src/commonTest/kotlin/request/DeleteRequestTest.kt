package request

import io.github.jan.supabase.postgrest.PropertyConversionMethod
import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.postgrest.query.request.DeleteRequestBuilder
import kotlin.test.Test
import kotlin.test.assertEquals

class DeleteRequestTest {

    private lateinit var sut: DeleteRequestBuilder

    @Test
    fun testDeleteRequestBuilder() {
        sut = DeleteRequestBuilder("public", PropertyConversionMethod.CAMEL_CASE_TO_SNAKE_CASE).apply {
            select()
            count(Count.EXACT)
        }

        assertEquals("DELETE", sut.httpMethod.value)
        assertEquals(setOf("return=representation", "count=exact"), sut.buildPrefer())
        assertEquals("public", sut.schema)
    }

}
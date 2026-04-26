package request

import io.github.jan.supabase.postgrest.PropertyConversionMethod
import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.postgrest.query.request.UpdateRequestBuilder
import io.ktor.http.HttpMethod
import kotlin.test.Test
import kotlin.test.assertEquals

class UpdateRequestTest {

    private lateinit var sut: UpdateRequestBuilder

    @Test
    fun testUpdate() {
        sut = UpdateRequestBuilder("public", PropertyConversionMethod.CAMEL_CASE_TO_SNAKE_CASE).apply {
            count(Count.EXACT)
            select()
        }

        assertEquals(HttpMethod.Patch, sut.httpMethod)
        assertEquals(
            listOf(
                "return=representation",
                "count=exact"
            ), sut.buildPrefer()
        )
        assertEquals("public", sut.schema)
    }


}
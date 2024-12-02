package request

import io.supabase.postgrest.query.Count
import io.supabase.postgrest.request.PostgrestRequest
import io.supabase.postgrest.request.RpcRequest
import io.ktor.http.HttpMethod
import kotlinx.serialization.json.JsonArray
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class RpcRequestTest {

    private lateinit var sut: PostgrestRequest

    @Test
    fun testCreateRpcRequest_isHead_thenReturnCorrectValue() {
        sut = RpcRequest(
            method = HttpMethod.Head,
            count = Count.EXACT,
            body = JsonArray(listOf()),
            urlParams = mapOf("Key1" to "Value1"),
            schema = "mySchema"
        )

        val count = (sut as RpcRequest).count
        assertNotNull(count)
        assertEquals("exact", count.identifier)

        assertEquals("HEAD", sut.method.value)
        assertEquals(
            listOf(
                "count=exact"
            ), sut.prefer
        )
        assertEquals("mySchema", sut.schema)
        assertEquals(mapOf("Key1" to "Value1"), sut.urlParams)
        assertEquals(JsonArray(listOf()), sut.body)
    }

    @Test
    fun testCreateRpcRequest_notHead_thenReturnCorrectValue() {
        sut = RpcRequest(
            method = HttpMethod.Post,
            count = Count.EXACT,
            body = JsonArray(listOf()),
            urlParams = mapOf("Key1" to "Value1"),
            schema = "mySchema"
        )
        val count = (sut as RpcRequest).count
        assertNotNull(count)
        assertEquals("exact", count.identifier)
        assertEquals("POST", sut.method.value)
        assertEquals(
            listOf(
                "count=exact"
            ), sut.prefer
        )
        assertEquals("mySchema", sut.schema)
        assertEquals(mapOf("Key1" to "Value1"), sut.urlParams)
        assertEquals(JsonArray(listOf()), sut.body)
    }

    @Test
    fun testCreateRpcRequest_withoutCount_thenReturnCorrectValue() {
        sut = RpcRequest(
            method = HttpMethod.Head,
            count = null,
            body = JsonArray(listOf()),
            urlParams = mapOf("Key1" to "Value1"),
            schema = "mySchema"
        )

        val count = (sut as RpcRequest).count
        assertNull(count)
        assertEquals("HEAD", sut.method.value)
        assertEquals(
            listOf(
            ), sut.prefer
        )
        assertEquals("mySchema", sut.schema)
        assertEquals(mapOf("Key1" to "Value1"), sut.urlParams)
        assertEquals(JsonArray(listOf()), sut.body)
    }

    @Test
    fun testCreateRpcRequest_withoutBody_thenReturnCorrectValue() {
        sut = RpcRequest(
            method = HttpMethod.Head,
            count = null,
            body = null,
            urlParams = mapOf("Key1" to "Value1"),
            schema = "mySchema"
        )

        assertEquals("HEAD", sut.method.value)
        assertEquals(
            listOf(
            ), sut.prefer
        )
        assertEquals("mySchema", sut.schema)
        assertEquals(mapOf("Key1" to "Value1"), sut.urlParams)
        assertNull(sut.body)
    }
    @Test
    fun testCreateRpcRequest_notHeadAndWithoutCount_thenReturnCorrectValue() {
        sut = RpcRequest(
            method = HttpMethod.Post,
            count = null,
            body = JsonArray(listOf()),
            urlParams = mapOf("Key1" to "Value1"),
            schema = "mySchema"
        )

        assertEquals("POST", sut.method.value)
        assertEquals(
            listOf(
            ), sut.prefer
        )
        assertEquals("mySchema", sut.schema)
        assertEquals(mapOf("Key1" to "Value1"), sut.urlParams)
        assertEquals(JsonArray(listOf()), sut.body)
    }

}
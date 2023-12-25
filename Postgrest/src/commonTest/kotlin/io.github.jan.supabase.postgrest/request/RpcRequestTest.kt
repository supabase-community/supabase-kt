package io.github.jan.supabase.postgrest.request

import io.github.jan.supabase.postgrest.query.Count
import kotlinx.serialization.json.JsonArray
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RpcRequestTest {

    private lateinit var sut: PostgrestRequest

    @Test
    fun testCreateRpcRequest_isHead_thenReturnCorrectValue() {
        sut = RpcRequest(
            head = true,
            count = Count.EXACT,
            body = JsonArray(listOf()),
            urlParams = mapOf("Key1" to "Value1"),
        )

        assertTrue((sut as RpcRequest).head)
        val count = (sut as RpcRequest).count
        assertNotNull(count)
        assertEquals("exact", count.identifier)

        assertEquals("HEAD", sut.method.value)
        assertEquals(
            listOf(
                "count=exact"
            ), sut.prefer
        )
        assertEquals("", sut.schema)
        assertEquals(mapOf("Key1" to "Value1"), sut.urlParams)
        assertEquals(JsonArray(listOf()), sut.body)
    }

    @Test
    fun testCreateRpcRequest_notHead_thenReturnCorrectValue() {
        sut = RpcRequest(
            head = false,
            count = Count.EXACT,
            body = JsonArray(listOf()),
            urlParams = mapOf("Key1" to "Value1"),
        )
        assertFalse((sut as RpcRequest).head)
        val count = (sut as RpcRequest).count
        assertNotNull(count)
        assertEquals("exact", count.identifier)
        assertEquals("POST", sut.method.value)
        assertEquals(
            listOf(
                "count=exact"
            ), sut.prefer
        )
        assertEquals("", sut.schema)
        assertEquals(mapOf("Key1" to "Value1"), sut.urlParams)
        assertEquals(JsonArray(listOf()), sut.body)
    }

    @Test
    fun testCreateRpcRequest_withoutCount_thenReturnCorrectValue() {
        sut = RpcRequest(
            head = true,
            count = null,
            body = JsonArray(listOf()),
            urlParams = mapOf("Key1" to "Value1"),
        )

        assertTrue((sut as RpcRequest).head)

        val count = (sut as RpcRequest).count
        assertNull(count)
        assertEquals("HEAD", sut.method.value)
        assertEquals(
            listOf(
            ), sut.prefer
        )
        assertEquals("", sut.schema)
        assertEquals(mapOf("Key1" to "Value1"), sut.urlParams)
        assertEquals(JsonArray(listOf()), sut.body)
    }

    @Test
    fun testCreateRpcRequest_withoutBody_thenReturnCorrectValue() {
        sut = RpcRequest(
            head = true,
            count = null,
            body = null,
            urlParams = mapOf("Key1" to "Value1"),
        )

        assertTrue((sut as RpcRequest).head)
        assertEquals("HEAD", sut.method.value)
        assertEquals(
            listOf(
            ), sut.prefer
        )
        assertEquals("", sut.schema)
        assertEquals(mapOf("Key1" to "Value1"), sut.urlParams)
        assertNull(sut.body)
    }
    @Test
    fun testCreateRpcRequest_notHeadAndWithoutCount_thenReturnCorrectValue() {
        sut = RpcRequest(
            head = false,
            count = null,
            body = JsonArray(listOf()),
            urlParams = mapOf("Key1" to "Value1"),
        )

        assertEquals("POST", sut.method.value)
        assertEquals(
            listOf(
            ), sut.prefer
        )
        assertEquals("", sut.schema)
        assertEquals(mapOf("Key1" to "Value1"), sut.urlParams)
        assertEquals(JsonArray(listOf()), sut.body)
    }

}
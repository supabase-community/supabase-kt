package io.github.jan.supabase.postgrest.request

import io.github.jan.supabase.postgrest.query.Count
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
            single = true,
            filter = mapOf("Key1" to listOf("Value1")),
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
        assertTrue(sut.single)
        assertEquals(mapOf("Key1" to listOf("Value1")), sut.filter)
        assertNull(sut.body)
        assertEquals(emptyMap(), sut.urlParams)
    }

    @Test
    fun testCreateSelectRequest_notHead_thenReturnCorrectValue() {
        sut = SelectRequest(
            head = false,
            count = Count.EXACT,
            single = true,
            filter = mapOf("Key1" to listOf("Value1")),
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
        assertTrue(sut.single)
        assertEquals(mapOf("Key1" to listOf("Value1")), sut.filter)
        assertNull(sut.body)
        assertEquals(emptyMap(), sut.urlParams)
    }

    @Test
    fun testCreateSelectRequest_notHeadAndWithoutCount_thenReturnCorrectValue() {
        sut = SelectRequest(
            head = false,
            count = null,
            single = true,
            filter = mapOf("Key1" to listOf("Value1")),
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
        assertTrue(sut.single)
        assertEquals(mapOf("Key1" to listOf("Value1")), sut.filter)
        assertNull(sut.body)
        assertEquals(emptyMap(), sut.urlParams)
    }

}
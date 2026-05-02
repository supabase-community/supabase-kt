package io.github.jan.supabase.postgrest.query

import io.github.jan.supabase.postgrest.PropertyConversionMethod
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

class PostgrestUpdateTest {

    @Serializable
    data class TestData(
        @SerialName("test_string") val testString: String,
        val testInt: Int,
        val testLong: Long,
        val testFloat: Float,
        val testDouble: Double,
        val testBoolean: Boolean,
        val testGeneric: TestInnerObj
    )

    @Serializable
    data class TestInnerObj(val innerMsg: String)

    private val serializer = KotlinXSerializer()

    @Test
    fun testSetToGeneric() {
        val update = buildPostgrestUpdate(serializer = serializer) {
            TestData::testGeneric setTo TestInnerObj("hello")
        }
        assertEquals(JsonObject(mapOf("test_generic" to JsonObject(mapOf("innerMsg" to JsonPrimitive("hello"))))), update)
    }

    @Test
    fun testSetToGenericNull() {
        val update = buildPostgrestUpdate(serializer = serializer) {
            TestData::testGeneric setTo null
        }
        assertEquals(JsonObject(mapOf("test_generic" to JsonNull)), update)
    }

    @Test
    fun testSetToString() {
        val update = buildPostgrestUpdate(serializer = serializer) {
            TestData::testString setTo "hello"
        }
        assertEquals(JsonObject(mapOf("test_string" to JsonPrimitive("hello"))), update)
    }
    
    @Test
    fun testSetToStringNull() {
        val update = buildPostgrestUpdate(serializer = serializer) {
            TestData::testString setTo null
        }
        assertEquals(JsonObject(mapOf("test_string" to JsonNull)), update)
    }

    @Test
    fun testSetToInt() {
        val update = buildPostgrestUpdate(serializer = serializer) {
            TestData::testInt setTo 42
        }
        assertEquals(JsonObject(mapOf("test_int" to JsonPrimitive(42))), update)
    }
    
    @Test
    fun testSetToIntNull() {
        val update = buildPostgrestUpdate(serializer = serializer) {
            TestData::testInt setTo null
        }
        assertEquals(JsonObject(mapOf("test_int" to JsonNull)), update)
    }

    @Test
    fun testSetToLong() {
        val update = buildPostgrestUpdate(serializer = serializer) {
            TestData::testLong setTo 42L
        }
        assertEquals(JsonObject(mapOf("test_long" to JsonPrimitive(42L))), update)
    }
    
    @Test
    fun testSetToLongNull() {
        val update = buildPostgrestUpdate(serializer = serializer) {
            TestData::testLong setTo null
        }
        assertEquals(JsonObject(mapOf("test_long" to JsonNull)), update)
    }

    @Test
    fun testSetToFloat() {
        val update = buildPostgrestUpdate(serializer = serializer) {
            TestData::testFloat setTo 42.0f
        }
        assertEquals(JsonObject(mapOf("test_float" to JsonPrimitive(42.0f))), update)
    }
    
    @Test
    fun testSetToFloatNull() {
        val update = buildPostgrestUpdate(serializer = serializer) {
            TestData::testFloat setTo null
        }
        assertEquals(JsonObject(mapOf("test_float" to JsonNull)), update)
    }

    @Test
    fun testSetToDouble() {
        val update = buildPostgrestUpdate(serializer = serializer) {
            TestData::testDouble setTo 42.0
        }
        assertEquals(JsonObject(mapOf("test_double" to JsonPrimitive(42.0))), update)
    }
    
    @Test
    fun testSetToDoubleNull() {
        val update = buildPostgrestUpdate(serializer = serializer) {
            TestData::testDouble setTo null
        }
        assertEquals(JsonObject(mapOf("test_double" to JsonNull)), update)
    }

    @Test
    fun testSetToBoolean() {
        val update = buildPostgrestUpdate(serializer = serializer) {
            TestData::testBoolean setTo true
        }
        assertEquals(JsonObject(mapOf("test_boolean" to JsonPrimitive(true))), update)
    }
    
    @Test
    fun testSetToBooleanNull() {
        val update = buildPostgrestUpdate(serializer = serializer) {
            TestData::testBoolean setTo null
        }
        assertEquals(JsonObject(mapOf("test_boolean" to JsonNull)), update)
    }

    @Test
    fun testSetColumnInt() {
        val update = buildPostgrestUpdate(serializer = serializer) {
            set("testInt", 42)
        }
        assertEquals(JsonObject(mapOf("testInt" to JsonPrimitive(42))), update)
    }

    @Test
    fun testSetColumnIntNull() {
        val update = buildPostgrestUpdate(serializer = serializer) {
            set("testInt", null as Int?)
        }
        assertEquals(JsonObject(mapOf("testInt" to JsonNull)), update)
    }

    @Test
    fun testSetColumnLong() {
        val update = buildPostgrestUpdate(serializer = serializer) {
            set("testLong", 42L)
        }
        assertEquals(JsonObject(mapOf("testLong" to JsonPrimitive(42L))), update)
    }

    @Test
    fun testSetColumnLongNull() {
        val update = buildPostgrestUpdate(serializer = serializer) {
            set("testLong", null as Long?)
        }
        assertEquals(JsonObject(mapOf("testLong" to JsonNull)), update)
    }

    @Test
    fun testSetColumnFloat() {
        val update = buildPostgrestUpdate(serializer = serializer) {
            set("testFloat", 42.0f)
        }
        assertEquals(JsonObject(mapOf("testFloat" to JsonPrimitive(42.0f))), update)
    }

    @Test
    fun testSetColumnFloatNull() {
        val update = buildPostgrestUpdate(serializer = serializer) {
            set("testFloat", null as Float?)
        }
        assertEquals(JsonObject(mapOf("testFloat" to JsonNull)), update)
    }

    @Test
    fun testSetColumnDouble() {
        val update = buildPostgrestUpdate(serializer = serializer) {
            set("testDouble", 42.0)
        }
        assertEquals(JsonObject(mapOf("testDouble" to JsonPrimitive(42.0))), update)
    }

    @Test
    fun testSetColumnDoubleNull() {
        val update = buildPostgrestUpdate(serializer = serializer) {
            set("testDouble", null as Double?)
        }
        assertEquals(JsonObject(mapOf("testDouble" to JsonNull)), update)
    }

    @Test
    fun testSetColumnBoolean() {
        val update = buildPostgrestUpdate(serializer = serializer) {
            set("testBoolean", true)
        }
        assertEquals(JsonObject(mapOf("testBoolean" to JsonPrimitive(true))), update)
    }

    @Test
    fun testSetColumnBooleanNull() {
        val update = buildPostgrestUpdate(serializer = serializer) {
            set("testBoolean", null as Boolean?)
        }
        assertEquals(JsonObject(mapOf("testBoolean" to JsonNull)), update)
    }

    @Test
    fun testSetToNullCol() {
        val update = buildPostgrestUpdate(serializer = serializer) {
            setToNull("testInt")
        }
        assertEquals(JsonObject(mapOf("testInt" to JsonNull)), update)
    }

    @Test
    fun testSetColumnGeneric() {
        val update = buildPostgrestUpdate(serializer = serializer) {
            set("testGeneric", TestInnerObj("hello"))
        }
        assertEquals(JsonObject(mapOf("testGeneric" to JsonObject(mapOf("innerMsg" to JsonPrimitive("hello"))))), update)
    }

    @Test
    fun testSetColumnGenericNull() {
        val update = buildPostgrestUpdate(serializer = serializer) {
            set("testGeneric", null as TestInnerObj?)
        }
        assertEquals(JsonObject(mapOf("testGeneric" to JsonNull)), update)
    }

    @Test
    fun testSetColumnGenericList() {
        val update = buildPostgrestUpdate(serializer = serializer) {
            set("testGenericList", listOf(TestInnerObj("hello")))
        }
        assertEquals(
            JsonObject(mapOf("testGenericList" to kotlinx.serialization.json.JsonArray(listOf(JsonObject(mapOf("innerMsg" to JsonPrimitive("hello"))))))),
            update
        )
    }

    @Test
    fun testSetColumnGenericListNull() {
        val update = buildPostgrestUpdate(serializer = serializer) {
            set("testGenericList", null as List<TestInnerObj>?)
        }
        assertEquals(JsonObject(mapOf("testGenericList" to JsonNull)), update)
    }

    @Test
    fun testSetColumnStringNull() {
        val update = buildPostgrestUpdate(serializer = serializer) {
            set("testString", null as String?)
        }
        assertEquals(JsonObject(mapOf("testString" to JsonNull)), update)
    }

    @Test
    fun testSetReifiedNull() {
        val update = buildPostgrestUpdate(serializer = serializer) {
            this.set<TestInnerObj>("test", null)
        }
        assertEquals(JsonObject(mapOf("test" to JsonNull)), update)
    }

    @Test
    fun testSetToNullGenericExplicit() {
        val update = buildPostgrestUpdate(serializer = serializer) {
            TestData::testGeneric.setTo(null)
        }
        assertEquals(JsonObject(mapOf("test_generic" to JsonNull)), update)
    }

    @Test
    fun testSetColumnGenericNullExplicit() {
        val update = buildPostgrestUpdate(serializer = serializer) {
            set<TestInnerObj>("testGeneric", null)
        }
        assertEquals(JsonObject(mapOf("testGeneric" to JsonNull)), update)
    }

}

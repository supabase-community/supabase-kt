import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.SupabaseClientBuilder
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.RpcMethod
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.result.PostgrestResult
import io.github.jan.supabase.testing.assertMethodIs
import io.github.jan.supabase.testing.assertPathIs
import io.github.jan.supabase.testing.createMockedSupabaseClient
import io.github.jan.supabase.testing.pathAfterVersion
import io.github.jan.supabase.testing.toJsonElement
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpMethod
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class PostgrestTest {

    private val configureClient: SupabaseClientBuilder.() -> Unit = {
        install(Postgrest)
    }
    
    private lateinit var supabase: SupabaseClient

    @Test
    fun testUrlLengthLimit() {
        supabase = createMockedSupabaseClient(
            configuration = configureClient
        ) {
            respond("")
        }
        runTest {
            assertFailsWith<IllegalStateException> {
                supabase.from("schema", "table").select {
                    filter {
                        eq("someKey", "a".repeat(8000)) //just to hit the limit
                    }
                }
            }
        }
    }

    @Test
    fun testSelect() {
        val columns = Columns.list("column1", "column2")
        testClient(
            request = { table ->
                from(table).select(columns)
            },
            requestHandler = {
                assertMethodIs(HttpMethod.Get, it.method)
                assertEquals(columns.value, it.url.parameters["select"])
                respond("")
            }
        )
    }

    @Test
    fun testSelectSchema() {
        val columns = Columns.list("column1", "column2")
        testClient(
            request = { table ->
                from("schema", table).select(columns)
            },
            requestHandler = {
                assertEquals("schema", it.headers["Accept-Profile"])
                assertMethodIs(HttpMethod.Get, it.method)
                assertEquals(columns.value, it.url.parameters["select"])
                respond("")
            }
        )
    }

    @Test
    fun testSelectHttpMethodHead() {
        val columns = Columns.list("column1", "column2")
        testClient(
            request = { table ->
                from(table).select(columns) {
                    head = true
                }
            },
            requestHandler = {
                assertMethodIs(HttpMethod.Head, it.method)
                assertEquals(columns.value, it.url.parameters["select"])
                respond("")
            }
        )
    }

    @Test
    fun testInsert() {
        val body = JsonArray(listOf(
            buildJsonObject {
                put("key", "value")
                put("key2", "value")
            }
        ))
        testClient(
            request = { table ->
                from("schema", table).insert(body) {
                    assertEquals(body, this.body)
                }
            },
            requestHandler = {
                assertMethodIs(HttpMethod.Post, it.method)
                val receivedBody = it.body.toJsonElement()
                assertEquals(body, receivedBody)
                assertEquals("key,key2", it.url.parameters["columns"])
                assertEquals("schema", it.headers["Content-Profile"])
                respond("")
            }
        )
    }

    @Test
    fun testUpsert() {
        val body = JsonArray(listOf(
            buildJsonObject {
                put("key", "value")
                put("key2", "value")
            }
        ))
        testClient(
            request = { table ->
                from("schema", table).upsert(body) {
                    assertEquals(body, this.body)
                }
            },
            requestHandler = {
                assertMethodIs(HttpMethod.Post, it.method)
                val receivedBody = it.body.toJsonElement()
                assertEquals(body, receivedBody)
                assertEquals("key,key2", it.url.parameters["columns"])
                assertEquals("schema", it.headers["Content-Profile"])
                respond("")
            }
        )
    }

    @Test
    fun testUpdate() {
        val body = buildJsonObject {
            put("key", "value")
            put("key2", "value")
        }
        testClient(
            request = { table ->
                from("schema", table).update(body) {
                    assertEquals(body, this.body)
                }
            },
            requestHandler = {
                assertMethodIs(HttpMethod.Patch, it.method)
                val receivedBody = it.body.toJsonElement()
                assertEquals(body, receivedBody)
                assertEquals("schema", it.headers["Content-Profile"])
                respond("")
            }
        )
    }

    @Test
    fun testUpdateManual() {
        testClient(
            request = { table ->
                from("schema", table).update({
                    set("key", "value")
                    set("key2", "value2")
                }) {
                    assertEquals(body, this.body)
                }
            },
            requestHandler = {
                assertMethodIs(HttpMethod.Patch, it.method)
                val receivedBody = it.body.toJsonElement().jsonObject
                assertEquals("value", receivedBody["key"]?.jsonPrimitive?.content)
                assertEquals("value2", receivedBody["key2"]?.jsonPrimitive?.content)
                assertEquals("schema", it.headers["Content-Profile"])
                respond("")
            }
        )
    }

    @Test
    fun testRpcNoParameters() {
        supabase = createMockedSupabaseClient(
            configuration = configureClient
        ) {
            assertPathIs("/rpc/function", it.url.pathAfterVersion())
            assertMethodIs(HttpMethod.Head, it.method)
            assertEquals("schema", it.headers["Accept-Profile"])
            respond("")
        }
        runTest {
            supabase.postgrest.rpc("function") {
                method = RpcMethod.HEAD
                schema = "schema"
            }
        }
    }

    @Test
    fun testRpcParameters() {
        val mockData = buildJsonObject {
            put("key", "value")
        }
        supabase = createMockedSupabaseClient(
            configuration = configureClient
        ) {
            assertPathIs("/rpc/function", it.url.pathAfterVersion())
            assertEquals(mockData, it.body.toJsonElement())
            assertMethodIs(HttpMethod.Post, it.method)
            assertEquals("schema", it.headers["Content-Profile"])
            respond("")
        }
        runTest {
            supabase.postgrest.rpc("function", mockData) {
                schema = "schema"
            }
        }
    }

    @Test
    fun testRpcReturnsNull() {
        supabase = createMockedSupabaseClient(
            configuration = configureClient
        ) {
            respond("null")
        }

        runTest {
            val result = supabase.postgrest.rpc("get_nullable_value")
            val value = result.decodeAs<String?>()
            assertNull(value)
        }
    }

    @Test
    fun testRpcReturnsStringNull() {
        supabase = createMockedSupabaseClient(
            configuration = configureClient
        ) {
            respond("\"null\"")
        }

        runTest {
            val result = supabase.postgrest.rpc("get_text_value")
            val value = result.decodeAs<String?>()
            assertEquals("null", value)
        }
    }

    /** Decode Truth Table **/

    @Serializable
    data class TestObject(val data: String) {
        companion object {
            val mock = TestObject("mockData")
        }
    }

    @Test
    fun testDecodeAsNullableTypeMatchingResponseAndResult() {
        supabase = createMockedSupabaseClient(configuration = configureClient) {
            respond(Json.encodeToString(TestObject.mock))
        }
        runTest {
            val result = supabase.postgrest.rpc("function")
            val value = result.decodeAs<TestObject?>()
            assertEquals(TestObject.mock, value)
        }
    }

    @Test
    fun testDecodeAsNullableTypeMismatchedResponseFailure() {
        supabase = createMockedSupabaseClient(configuration = configureClient) {
            respond("[]")
        }
        runTest {
            val result = supabase.postgrest.rpc("function")
            assertFailsWith<SerializationException> {
                result.decodeAs<TestObject?>()
            }
        }
    }

    @Test
    fun testDecodeAsNullableTypeNullResponseNullResult() {
        supabase = createMockedSupabaseClient(configuration = configureClient) {
            respond("null")
        }
        runTest {
            val result = supabase.postgrest.rpc("function")
            val value = result.decodeAs<TestObject?>()
            assertNull(value)
        }
    }

    private fun testClient(
        table: String = "table",
        request: suspend SupabaseClient.(table: String) -> PostgrestResult,
        requestHandler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData = { respond("")},
    ) {
        supabase = createMockedSupabaseClient(
            configuration = configureClient
        ) {
            assertPathIs("/$table", it.url.pathAfterVersion())
            requestHandler(it)
        }
        runTest {
            supabase.request(table)
        }
    }

    @AfterTest
    fun cleanup() {
        runTest {
            if(::supabase.isInitialized) {
                supabase.close()
            }
        }
    }

}
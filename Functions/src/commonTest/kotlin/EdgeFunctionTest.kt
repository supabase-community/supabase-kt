import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.FunctionRegion
import io.github.jan.supabase.functions.asFlow
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.testing.createMockedSupabaseClient
import io.github.jan.supabase.testing.pathAfterVersion
import io.github.jan.supabase.testing.toJsonElement
import io.ktor.client.engine.mock.respond
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import io.ktor.utils.io.readLine
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class EdgeFunctionTest {

    private lateinit var supabase: SupabaseClient
    
    @Test
    fun testInvokingWithoutBody() {
        runTest {
            val expectedName = "myFunction"
            val expectedRegion = FunctionRegion.EU_WEST_1
            val expectedHeaders = Headers.build {
                append("myHeader", "myValue")
            }
            supabase = createMockedSupabaseClient(
                configuration = configuration,
                requestHandler = {
                    assertEquals("POST", it.method.value)
                    assertEquals("/$expectedName", it.url.pathAfterVersion())
                    assertEquals(expectedRegion.value, it.headers["x-region"])
                    assertEquals("myValue", it.headers["myHeader"])
                    respond("")
                }
            )
            val function = supabase.functions.buildEdgeFunction(expectedName, expectedRegion, expectedHeaders)
            function()
        }
    }

    @Test
    fun testInvokingWithBody() {
        runTest {
            val expectedName = "myFunction"
            val expectedRegion = FunctionRegion.EU_WEST_1
            val expectedHeaders = Headers.build {
                set(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }
            val expectedBody = buildJsonObject {
                put("key", "value")
            }
            supabase = createMockedSupabaseClient(
                configuration = configuration,
                requestHandler = {
                    val body = it.body.toJsonElement().jsonObject
                    assertEquals(expectedBody, body)
                    assertEquals("POST", it.method.value)
                    assertEquals("/$expectedName", it.url.pathAfterVersion())
                    assertEquals(expectedRegion.value, it.headers["x-region"])
                    respond("")
                }
            )
            val function = supabase.functions.buildEdgeFunction(expectedName, expectedRegion, expectedHeaders)
            function(expectedBody)
        }
    }

    @Test
    fun testInvokeStreaming() {
        runTest {
            val expectedName = "myFunction"
            val expectedRegion = FunctionRegion.EU_WEST_1
            val expectedHeaders = Headers.build {
                append("myHeader", "myValue")
            }
            val sseContent = "data: hello\ndata: world"
            supabase = createMockedSupabaseClient(
                configuration = configuration,
                requestHandler = {
                    assertEquals("POST", it.method.value)
                    assertEquals("/$expectedName", it.url.pathAfterVersion())
                    assertEquals(expectedRegion.value, it.headers["x-region"])
                    assertEquals("myValue", it.headers["myHeader"])
                    respond(
                        content = sseContent,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Text.EventStream.toString())
                    )
                }
            )
            val function = supabase.functions.buildEdgeFunction(expectedName, expectedRegion, expectedHeaders)
            val channel = function.invokeStreaming()
            assertEquals("data: hello", channel.readLine())
            assertEquals("data: world", channel.readLine())
        }
    }

    @Test
    fun testInvokeStreamingAsFlow() {
        runTest {
            val expectedName = "myFunction"
            val expectedRegion = FunctionRegion.EU_WEST_1
            val sseContent = "data: hello\ndata: world"
            supabase = createMockedSupabaseClient(
                configuration = configuration,
                requestHandler = {
                    respond(
                        content = sseContent,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Text.EventStream.toString())
                    )
                }
            )
            val function = supabase.functions.buildEdgeFunction(expectedName, expectedRegion)
            val lines = function.invokeStreaming().asFlow().toList()
            assertEquals(listOf("data: hello", "data: world"), lines)
        }
    }

    @Test
    fun testPrepareInvoke() {
        runTest {
            val expectedName = "myFunction"
            val expectedRegion = FunctionRegion.EU_WEST_1
            supabase = createMockedSupabaseClient(
                configuration = configuration,
                requestHandler = {
                    assertEquals("POST", it.method.value)
                    assertEquals("/$expectedName", it.url.pathAfterVersion())
                    assertEquals(expectedRegion.value, it.headers["x-region"])
                    respond("streaming content")
                }
            )
            val function = supabase.functions.buildEdgeFunction(expectedName, expectedRegion)
            val statement = function.prepareInvoke()
            statement.execute { response ->
                assertEquals("streaming content", response.bodyAsText())
            }
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
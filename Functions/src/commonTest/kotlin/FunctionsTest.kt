import io.github.jan.supabase.SupabaseClientBuilder
import io.github.jan.supabase.functions.FunctionRegion
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.testing.createMockedSupabaseClient
import io.github.jan.supabase.testing.pathAfterVersion
import io.github.jan.supabase.testing.toJsonElement
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals

class FunctionsTest {

    private val configuration: SupabaseClientBuilder.() -> Unit = {
        install(Functions)
    }

    @Test
    fun testInvokingWithoutBody() {
        runTest {
            val expectedName = "myFunction"
            val expectedRegion = FunctionRegion.EU_WEST_1
            val expectedHeaders = Headers.build {
                append("myHeader", "myValue")
            }
            val supabase = createMockedSupabaseClient(
                configuration = configuration,
                requestHandler = {
                    assertEquals("POST", it.method.value)
                    assertEquals("/$expectedName", it.url.pathAfterVersion())
                    assertEquals(expectedRegion.value, it.headers["x-region"])
                    assertEquals("myValue", it.headers["myHeader"])
                    respond("")
                }
            )
            supabase.functions.invoke(
                function = expectedName,
                region = expectedRegion,
                headers = expectedHeaders
            )
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
            val supabase = createMockedSupabaseClient(
                configuration = configuration,
                requestHandler = {
                    val body = it.body.toJsonElement().jsonObject
                    assertEquals(expectedBody, body)
                    assertEquals("POST", it.method.value)
                    assertEquals("/$expectedName", it.url.pathAfterVersion())
                    assertEquals(expectedRegion.value, it.headers["x-region"])
                    println(it.headers)
                    assertEquals(ContentType.Application.Json.toString(), it.headers[HttpHeaders.ContentType])
                    respond("")
                }
            )
            supabase.functions.invoke(
                function = expectedName,
                region = expectedRegion,
                body = expectedBody,
                headers = expectedHeaders
            )
        }
    }

}
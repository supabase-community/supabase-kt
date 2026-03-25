import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.SupabaseClientBuilder
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.minimalConfig
import io.github.jan.supabase.functions.FunctionRegion
import io.github.jan.supabase.functions.Functions
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

internal val configuration: SupabaseClientBuilder.() -> Unit = {
    install(Functions)
}

class FunctionsTest {

    private lateinit var supabase: SupabaseClient
    
    @Test
    fun testAuthorizationHeaderAuth() {
        runTest {
            val expectedJWT = "jwt"
            supabase = createMockedSupabaseClient(
                configuration ={
                    install(Auth) {
                        minimalConfig()
                    }
                    configuration()
                },
                requestHandler = {
                    assertEquals("Bearer $expectedJWT", it.headers[HttpHeaders.Authorization])
                    respond("")
                }
            )
            supabase.auth.awaitInitialization()
            supabase.auth.importAuthToken(expectedJWT)
            supabase.functions.invoke(
                function = ""
            )
        }
    }

    @Test
    fun testAuthorizationHeaderCustomToken() {
        runTest {
            val expectedJWT = "jwt"
            supabase = createMockedSupabaseClient(
                configuration = {
                    install(Functions) {
                        jwtToken = expectedJWT
                    }
                },
                requestHandler = {
                    assertEquals("Bearer $expectedJWT", it.headers[HttpHeaders.Authorization])
                    respond("")
                }
            )
            supabase.functions.invoke(
                function = ""
            )
        }
    }

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
            supabase.functions.invoke(
                function = expectedName,
                region = expectedRegion,
                body = expectedBody,
                headers = expectedHeaders
            )
        }
    }

    @Test
    fun testInvokeStreaming() {
        runTest {
            val expectedName = "myFunction"
            val expectedRegion = FunctionRegion.EU_WEST_1
            val sseContent = "data: hello\ndata: world\ndata: done"
            supabase = createMockedSupabaseClient(
                configuration = configuration,
                requestHandler = {
                    assertEquals("POST", it.method.value)
                    assertEquals("/$expectedName", it.url.pathAfterVersion())
                    assertEquals(expectedRegion.value, it.headers["x-region"])
                    respond(
                        content = sseContent,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Text.EventStream.toString())
                    )
                }
            )
            val channel = supabase.functions.invokeStreaming(
                function = expectedName,
                region = expectedRegion
            )
            assertEquals("data: hello", channel.readLine())
            assertEquals("data: world", channel.readLine())
            assertEquals("data: done", channel.readLine())
        }
    }

    @Test
    fun testInvokeStreamingAsFlow() {
        runTest {
            val expectedName = "myFunction"
            val sseContent = "data: hello\ndata: world\ndata: done"
            supabase = createMockedSupabaseClient(
                configuration = configuration,
                requestHandler = {
                    respond(
                        content = sseContent,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Text.EventStream.toString())
                    )
                }
            )
            val lines = supabase.functions.invokeStreaming(
                function = expectedName
            ).asFlow().toList()
            assertEquals(listOf("data: hello", "data: world", "data: done"), lines)
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
            val statement = supabase.functions.prepareInvoke(
                function = expectedName,
                region = expectedRegion
            )
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
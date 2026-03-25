package io.github.jan.supabase.integration

import io.github.jan.supabase.functions.asFlow
import io.github.jan.supabase.functions.functions
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.headers
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class FunctionsIntegrationTest : IntegrationTestBase() {

    @Test
    fun `invoke echo function returns the same body`() = runTest {
        val client = createAuthenticatedClient()
        val requestBody = buildJsonObject {
            put("message", "hello")
        }
        val response = client.functions.invoke(
            function = "echo",
            body = requestBody,
            headers = headers {
                append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }
        )
        assertEquals("""{"message":"hello"}""", response.bodyAsText())
    }

    @Test
    fun `invoke streaming function returns SSE lines`() = runTest {
        val client = createAuthenticatedClient()
        val lines = client.functions.invokeStreaming("sse").asFlow().toList()
        assertEquals(listOf("data: hello", "", "data: world", "", "data: done", ""), lines)
    }
}

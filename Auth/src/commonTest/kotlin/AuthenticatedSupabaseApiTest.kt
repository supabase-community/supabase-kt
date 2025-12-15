import io.github.jan.supabase.SupabaseClientBuilder
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.AuthenticatedApiConfig
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.authenticatedSupabaseApi
import io.github.jan.supabase.auth.exception.SessionRequiredException
import io.github.jan.supabase.auth.minimalConfig
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.testing.createMockedSupabaseClient
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.header
import io.ktor.http.HttpMethod
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class AuthenticatedSupabaseApiTest {

    private val configuration: SupabaseClientBuilder.() -> Unit = {
        install(Auth) {
            minimalConfig()
        }
    }

    @Test
    fun testRequestWithBearerToken() = runTest {
        val expectedToken = "test-access-token"
        val expectedUrl = "test.url.de/"

        val client = createMockedSupabaseClient(configuration = configuration) {
            assertEquals("Bearer $expectedToken", it.headers["Authorization"])
            respond("")
        }

        client.auth.importSession(createSession(expectedToken))

        val api = client.authenticatedSupabaseApi(
            resolveUrl = { expectedUrl + it },
            config = AuthenticatedApiConfig(
                jwtToken = null,
                defaultRequest = null,
                requireSession = true
            )
        )

        api.get("test")
    }

    @Test
    fun testRequestWithCustomHeaders() = runTest {
        val expectedToken = "test-access-token"
        val expectedHeaderName = "X-Custom-Header"
        val expectedHeaderValue = "custom-value"

        val client = createMockedSupabaseClient(configuration = configuration) {
            assertEquals("Bearer $expectedToken", it.headers["Authorization"])
            assertEquals(expectedHeaderValue, it.headers[expectedHeaderName])
            respond("")
        }

        client.auth.importSession(createSession(expectedToken))

        val api = client.authenticatedSupabaseApi(
            resolveUrl = { "test.url.de/$it" },
            config = AuthenticatedApiConfig(
                jwtToken = null,
                defaultRequest = {
                    header(expectedHeaderName, expectedHeaderValue)
                },
                requireSession = true
            )
        )

        api.get("test")
    }

    @Test
    fun testRequestWithCustomJwtToken() = runTest {
        val customToken = "custom-jwt-token"

        val client = createMockedSupabaseClient(configuration = configuration) {
            assertEquals("Bearer $customToken", it.headers["Authorization"])
            respond("")
        }

        client.auth.importSession(createSession("different-token"))

        val api = client.authenticatedSupabaseApi(
            resolveUrl = { "test.url.de/$it" },
            config = AuthenticatedApiConfig(
                jwtToken = customToken,
                defaultRequest = null,
                requireSession = true
            )
        )

        api.get("test")
    }

    @Test
    fun testRequestWithoutSessionThrowsException() = runTest {
        val client = createMockedSupabaseClient(configuration = configuration) {
            respond("")
        }

        client.auth.awaitInitialization()

        val api = client.authenticatedSupabaseApi(
            resolveUrl = { "test.url.de/$it" },
            config = AuthenticatedApiConfig(
                jwtToken = null,
                defaultRequest = null,
                requireSession = true
            )
        )

        assertFailsWith<SessionRequiredException> {
            api.get("test")
        }
    }

    @Test
    fun testRequestWithoutSessionButNotRequiredUsesApiKey() = runTest {
        val apiKey = "test-api-key"

        val client = createMockedSupabaseClient(
            supabaseKey = apiKey,
            configuration = configuration
        ) {
            assertEquals("Bearer $apiKey", it.headers["Authorization"])
            respond("")
        }

        client.auth.awaitInitialization()

        val api = client.authenticatedSupabaseApi(
            resolveUrl = { "test.url.de/$it" },
            config = AuthenticatedApiConfig(
                jwtToken = null,
                defaultRequest = null,
                requireSession = false
            )
        )

        api.get("test")
    }

    @Test
    fun testUrlResolution() = runTest {
        val baseUrl = "https://api.example.com"
        val path = "endpoint/test"
        var requestUrl = ""

        val client = createMockedSupabaseClient(configuration = configuration) {
            requestUrl = it.url.toString()
            respond("")
        }

        client.auth.importSession(createSession("test-token"))

        val api = client.authenticatedSupabaseApi(
            resolveUrl = { "$baseUrl/$it" },
            config = AuthenticatedApiConfig(
                jwtToken = null,
                defaultRequest = null,
                requireSession = true
            )
        )

        api.get(path)

        assertTrue(requestUrl.contains(baseUrl))
        assertTrue(requestUrl.contains(path))
    }

    @Test
    fun testRawRequestWithoutPath() = runTest {
        val baseUrl = "https://api.example.com"
        var requestUrl = ""

        val client = createMockedSupabaseClient(configuration = configuration) {
            requestUrl = it.url.toString()
            respond("")
        }

        client.auth.importSession(createSession("test-token"))

        val api = client.authenticatedSupabaseApi(
            resolveUrl = { "$baseUrl/$it" },
            config = AuthenticatedApiConfig(
                jwtToken = null,
                defaultRequest = null,
                requireSession = true
            )
        )

        api.get("")

        assertTrue(requestUrl.contains(baseUrl))
    }

    @Test
    fun testPrepareRequest() = runTest {
        val expectedToken = "test-access-token"

        val client = createMockedSupabaseClient(configuration = configuration) {
            assertEquals("Bearer $expectedToken", it.headers["Authorization"])
            assertEquals(HttpMethod.Post, it.method)
            respond("")
        }

        client.auth.importSession(createSession(expectedToken))

        val api = client.authenticatedSupabaseApi(
            resolveUrl = { "test.url.de/$it" },
            config = AuthenticatedApiConfig(
                jwtToken = null,
                defaultRequest = null,
                requireSession = true
            )
        )

        val statement = api.prepareRequest("test") {
            method = HttpMethod.Post
        }
        statement.execute()
    }

    @Test
    fun testPrepareRequestWithExpiredSession() = runTest {
        val client = createMockedSupabaseClient(configuration = {
            install(Auth) {
                minimalConfig()
                alwaysAutoRefresh = false
            }
        }) {
            respond("")
        }

        client.auth.awaitInitialization()
        client.auth.importSession(createSession("expired-token", expiresIn = 0))

        val api = client.authenticatedSupabaseApi(
            resolveUrl = { "test.url.de/$it" },
            config = AuthenticatedApiConfig(
                jwtToken = null,
                defaultRequest = null,
                requireSession = true
            )
        )

        val statement = api.prepareRequest("test") {
            method = HttpMethod.Get
        }
        statement.execute()
    }

    private fun createSession(
        accessToken: String,
        expiresIn: Long = 3600,
        refreshToken: String = "refresh-token"
    ) = UserSession(
        accessToken = accessToken,
        refreshToken = refreshToken,
        expiresIn = expiresIn,
        tokenType = "Bearer",
        user = null
    )

}
package io.github.jan.supabase.integration.auth

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.minimalConfig
import io.github.jan.supabase.auth.oauth.OAuthAuthorizationDetails
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.integration.IntegrationTestBase
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.Url
import kotlinx.coroutines.test.runTest
import okio.ByteString.Companion.toByteString
import org.junit.jupiter.api.Test
import kotlin.io.encoding.Base64
import kotlin.random.Random
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class OAuthConsentApiTest: IntegrationTestBase() {

    private fun createServiceRoleClient() = createSupabaseClient(supabaseUrl, supabaseServiceRoleKey) {
        install(Auth) {
            minimalConfig()
        }
        install(Postgrest)
        install(Storage)
    }

    private fun createHttpClient() = HttpClient {
        followRedirects = false
    }

    @Test
    fun testGetAuthorizationDetails() = runTest {
        val client = createAuthenticatedClient()
        startOAuthFlow {
            val oauthResponse = client.auth.oauth.getAuthorizationDetails(it)
            assertIs<OAuthAuthorizationDetails>(oauthResponse)
        }
    }

    private suspend fun startOAuthFlow(callback: suspend (String) -> Unit) {
        val httpClient = createHttpClient()
        val serviceRoleClient = createServiceRoleClient()
        val oauthClient = serviceRoleClient.auth.admin.oauth.createClient {
            clientName = "Integration Test App"
            redirectUris = listOf("https://example.com/callback")
        }
        try {
            val authorizationUrl = "$supabaseUrl/auth/v1/oauth/authorize"
            val response = httpClient.get(authorizationUrl) {
                parameter("client_id", oauthClient.clientId)
                parameter("redirect_uri", "https://example.com/callback")
                parameter("code_challenge_method", "s256")
                parameter("code_challenge", generateCodeChallenge(generateCodeVerifier()))
                parameter("scope", "openid")
            }
            val location = response.headers["Location"]
            assertNotNull(location)
            val authorizationId = Url(location).parameters["authorization_id"]
            assertNotNull(authorizationId)
            callback(authorizationId)
        } finally {
            serviceRoleClient.auth.admin.oauth.deleteClient(oauthClient.clientId)
            httpClient.close()
        }
    }

    internal fun generateCodeVerifier(): String {
        val bytes = ByteArray(64)
        Random.nextBytes(bytes)
        return Base64.UrlSafe.encode(bytes)
    }

    internal fun generateCodeChallenge(codeVerifier: String): String {
        val byteString = codeVerifier.encodeToByteArray().toByteString()
        val hash = byteString.sha256()
        return Base64.UrlSafe.encode(hash.toByteArray()).replace("=", "")
    }

}
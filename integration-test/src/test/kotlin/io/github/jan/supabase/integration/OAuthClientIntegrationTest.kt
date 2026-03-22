package io.github.jan.supabase.integration

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.admin.oauth.OAuthClientTokenEndpointAuthMethod
import io.github.jan.supabase.auth.admin.oauth.OAuthClientType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.minimalConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration tests for the OAuth 2.1 client admin API.
 *
 * Requirements:
 * - GoTrue >= v2.188.0 (the admin/oauth/clients endpoints don't exist in earlier versions)
 * - GOTRUE_OAUTH_SERVER_ENABLED=true environment variable set on the GoTrue container
 *
 * As of Supabase CLI v2.78.1, the bundled GoTrue is v2.187.0 which does not support these
 * endpoints. To run these tests locally:
 *
 * 1. Pull a newer GoTrue image:
 *    docker pull public.ecr.aws/supabase/gotrue:v2.188.1
 *    docker tag public.ecr.aws/supabase/gotrue:v2.188.1 public.ecr.aws/supabase/gotrue:v2.187.0
 *
 * 2. Start Supabase, then restart the auth container with the feature flag:
 *    supabase start --workdir integration-test
 *    docker stop supabase_auth_supabase-kt-tests && docker rm supabase_auth_supabase-kt-tests
 *    docker run -d --name supabase_auth_supabase-kt-tests \
 *      --network supabase_network_supabase-kt-tests \
 *      --env-file <(docker inspect supabase_auth_supabase-kt-tests | jq -r '.[0].Config.Env[]') \
 *      -e GOTRUE_OAUTH_SERVER_ENABLED=true \
 *      public.ecr.aws/supabase/gotrue:v2.187.0 gotrue
 *
 * These tests were verified against GoTrue v2.188.1 with GOTRUE_OAUTH_SERVER_ENABLED=true.
 */
@Disabled("Requires GoTrue >= v2.188.0 with GOTRUE_OAUTH_SERVER_ENABLED=true. Remove when CI supports it.")
class OAuthClientIntegrationTest : IntegrationTestBase() {

    private fun createServiceRoleClient() = createSupabaseClient(supabaseUrl, supabaseServiceRoleKey) {
        install(Auth) {
            minimalConfig()
        }
        install(Postgrest)
        install(Storage)
    }

    @Test
    fun testCreateAndGetClient() = runTest {
        val client = createServiceRoleClient()
        val created = client.auth.admin.oauth.createClient {
            clientName = "Integration Test App"
            redirectUris = listOf("https://example.com/callback")
        }

        assertNotNull(created.clientId)
        assertNotNull(created.clientSecret)
        assertEquals("Integration Test App", created.clientName)
        assertEquals(OAuthClientType.CONFIDENTIAL, created.clientType)
        assertTrue(created.redirectUris.contains("https://example.com/callback"))

        val fetched = client.auth.admin.oauth.getClient(created.clientId)
        assertEquals(created.clientId, fetched.clientId)
        assertEquals("Integration Test App", fetched.clientName)

        // Cleanup
        client.auth.admin.oauth.deleteClient(created.clientId)
    }

    @Test
    fun testListClients() = runTest {
        val client = createServiceRoleClient()
        val created = client.auth.admin.oauth.createClient {
            clientName = "List Test App"
            redirectUris = listOf("https://example.com/callback")
        }

        try {
            val clients = client.auth.admin.oauth.listClients()
            assertTrue(clients.isNotEmpty())
            assertTrue(clients.any { it.clientId == created.clientId })
        } finally {
            client.auth.admin.oauth.deleteClient(created.clientId)
        }
    }

    @Test
    fun testUpdateClient() = runTest {
        val client = createServiceRoleClient()
        val created = client.auth.admin.oauth.createClient {
            clientName = "Before Update"
            redirectUris = listOf("https://example.com/callback")
        }

        try {
            val updated = client.auth.admin.oauth.updateClient(created.clientId) {
                clientName = "After Update"
            }
            assertEquals("After Update", updated.clientName)
            assertEquals(created.clientId, updated.clientId)
        } finally {
            client.auth.admin.oauth.deleteClient(created.clientId)
        }
    }

    @Test
    fun testDeleteClient() = runTest {
        val client = createServiceRoleClient()
        val created = client.auth.admin.oauth.createClient {
            clientName = "To Delete"
            redirectUris = listOf("https://example.com/callback")
        }

        client.auth.admin.oauth.deleteClient(created.clientId)

        assertFailsWith<RestException> {
            client.auth.admin.oauth.getClient(created.clientId)
        }
    }

    @Test
    fun testRegenerateClientSecret() = runTest {
        val client = createServiceRoleClient()
        val created = client.auth.admin.oauth.createClient {
            clientName = "Regenerate Secret Test"
            redirectUris = listOf("https://example.com/callback")
        }

        try {
            val regenerated = client.auth.admin.oauth.regenerateClientSecret(created.clientId)
            assertEquals(created.clientId, regenerated.clientId)
            assertNotNull(regenerated.clientSecret)
            // Secret should be different after regeneration
            assertTrue(created.clientSecret != regenerated.clientSecret)
        } finally {
            client.auth.admin.oauth.deleteClient(created.clientId)
        }
    }

    @Test
    fun testTokenEndpointAuthMethod() = runTest {
        val client = createServiceRoleClient()
        val created = client.auth.admin.oauth.createClient {
            clientName = "Auth Method Test"
            redirectUris = listOf("https://example.com/callback")
            tokenEndpointAuthMethod = OAuthClientTokenEndpointAuthMethod.CLIENT_SECRET_POST
        }

        try {
            assertEquals(OAuthClientTokenEndpointAuthMethod.CLIENT_SECRET_POST, created.tokenEndpointAuthMethod)
        } finally {
            client.auth.admin.oauth.deleteClient(created.clientId)
        }
    }
}

package io.github.jan.supabase.integration

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach

abstract class IntegrationTestBase {

    companion object {
        val supabaseUrl: String = System.getenv("SUPABASE_URL") ?: "http://127.0.0.1:54321"
        val supabaseAnonKey: String = System.getenv("SUPABASE_ANON_KEY")
            ?: error("SUPABASE_ANON_KEY environment variable is required")
        val supabaseServiceRoleKey: String = System.getenv("SUPABASE_SERVICE_ROLE_KEY")
            ?: error("SUPABASE_SERVICE_ROLE_KEY environment variable is required")
    }

    private val clients = mutableListOf<SupabaseClient>()

    fun createTestClient(
        key: String = supabaseAnonKey,
        configure: io.github.jan.supabase.SupabaseClientBuilder.() -> Unit = {}
    ): SupabaseClient {
        val client = createSupabaseClient(supabaseUrl, key) {
            install(Auth) {
                alwaysAutoRefresh = false
            }
            install(Postgrest)
            install(Storage)
            configure()
        }
        clients.add(client)
        return client
    }

    suspend fun createAuthenticatedClient(): SupabaseClient {
        val client = createTestClient()
        val email = "test-${System.nanoTime()}@example.com"
        val password = "test-password-123!"
        client.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
        return client
    }

    @AfterEach
    fun closeClients() = runBlocking {
        clients.forEach { it.close() }
        clients.clear()
    }
}

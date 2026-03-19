package io.github.jan.supabase.integration

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

fun createServiceRoleClient(): SupabaseClient {
    return createSupabaseClient(
        IntegrationTestBase.supabaseUrl,
        IntegrationTestBase.supabaseServiceRoleKey
    ) {
        install(Auth) {
            alwaysAutoRefresh = false
        }
        install(Postgrest)
        install(Storage)
    }
}

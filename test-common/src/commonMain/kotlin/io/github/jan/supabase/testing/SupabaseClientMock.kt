package io.github.jan.supabase.testing

import io.supabase.SupabaseClient
import io.supabase.SupabaseClientBuilder
import io.supabase.createSupabaseClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData

fun createMockedSupabaseClient(
    supabaseUrl: String = "https://projectref.supabase.co",
    supabaseKey: String = "project-anon-key",
    configuration: SupabaseClientBuilder.() -> Unit = {},
    requestHandler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData = { respond("") },
): SupabaseClient {
    return createSupabaseClient(
        supabaseUrl = supabaseUrl,
        supabaseKey = supabaseKey,
    ) {
        httpEngine = MockEngine {
            requestHandler(this, it)
        }
        configuration()
    }
}
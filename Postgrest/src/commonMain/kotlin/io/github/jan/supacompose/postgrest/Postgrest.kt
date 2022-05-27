package io.github.jan.supacompose.postgrest

import io.github.jan.supacompose.SupabaseClient
import io.github.jan.supacompose.plugins.SupabasePlugin
import io.github.jan.supacompose.postgrest.query.PostgrestBuilder

sealed interface PostgrestClient {

    suspend fun from(table: String): PostgrestBuilder

    class Config

    companion object : SupabasePlugin<Config, PostgrestClient> {

        override val key = "postgrest"

        override fun create(supabaseClient: SupabaseClient, config: Config.() -> Unit): PostgrestClient {
            return PostgrestClientImpl(supabaseClient)
        }

    }

}

internal class PostgrestClientImpl(private val supabaseClient: SupabaseClient) : PostgrestClient {

    override suspend fun from(table: String): PostgrestBuilder {
        return PostgrestBuilder(supabaseClient, table)
    }

}

val SupabaseClient.postgrest: PostgrestClient
    get() = plugins.getOrElse("postgrest") {
        throw IllegalStateException("Postgres plugin not installed")
    } as? PostgrestClient ?: throw IllegalStateException("Postgres plugin not installed")
package io.github.jan.supacompose.postgrest

import io.github.jan.supacompose.SupabaseClient
import io.github.jan.supacompose.plugins.SupacomposePlugin
import io.github.jan.supacompose.postgrest.query.PostgrestBuilder

sealed interface Postgrest {

    fun from(table: String): PostgrestBuilder

    operator fun get(table: String): PostgrestBuilder = from(table)

    class Config

    companion object : SupacomposePlugin<Config, Postgrest> {

        override val key = "postgrest"

        override fun createConfig(init: Config.() -> Unit) = Config().apply(init)
        override fun create(supabaseClient: SupabaseClient, config: Config): Postgrest {
            return PostgrestImpl(supabaseClient)
        }

    }

}

internal class PostgrestImpl(private val supabaseClient: SupabaseClient) : Postgrest {

    override fun from(table: String): PostgrestBuilder {
        return PostgrestBuilder(supabaseClient, table)
    }

}

val SupabaseClient.postgrest: Postgrest
    get() = plugins.getOrElse("postgrest") {
        throw IllegalStateException("Postgres plugin not installed")
    } as? Postgrest ?: throw IllegalStateException("Postgres plugin not installed")
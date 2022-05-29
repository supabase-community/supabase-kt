package io.github.jan.supacompose.plugins

import io.github.jan.supacompose.SupabaseClient
import io.github.jan.supacompose.SupabaseClientBuilder

interface SupacomposePlugin<C, O> {

    val key: String

    fun setup(builder: SupabaseClientBuilder, config: C.() -> Unit) {}
    fun create(supabaseClient: SupabaseClient, config: C.() -> Unit) : O

}
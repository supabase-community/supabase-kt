package io.github.jan.supacompose.plugins

import io.github.jan.supacompose.SupabaseClient

interface SupabasePlugin<C, O> {

    val key: String

    fun create(supabaseClient: SupabaseClient, config: C.() -> Unit) : O

}